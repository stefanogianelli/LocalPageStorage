package support;

import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.naming.Context;
import javax.naming.NamingException;

import downloadImages.DownloadImages;
import parsePage.ParsePage;
import storePage.StorePage;
import loadUrl.LoadURL;
import modifyPage.ModifyPage;

public class Monitoring {
	
	private static final int THREADS = 12;
	private static final int THRESHOLD = 2;
	private static final int TIMEOUT = 5000;

	private static LoadURL [] loadUrl;
	private static StorePage [] storePage;
	private static ParsePage [] parsePage;
	private static DownloadImages [] downloadImages;
	private static ModifyPage [] modifyPage;
	private AmazonS3ws aws;
	private ExecutorService executor;
	private Context initialContext;
	private JMSContext jmsContext;
	
	public static void main(String[] args) throws NamingException, JMSException, InterruptedException {		
		Monitoring monitor = new Monitoring();
		monitor.startComponents();
	}	
	
	public Monitoring () throws NamingException {
		//solve the trustAnchor problem
		System.setProperty("javax.net.ssl.trustStore","C:\\glassfish4\\glassfish\\domains\\domain1\\config\\cacerts.jks");
		//create the context
		initialContext = Support.getContext();
		ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("java:comp/DefaultJMSConnectionFactory");
		jmsContext = cf.createContext();
		//create instance of Amazon S3
		aws = new AmazonS3ws();
		//create the executor
		executor = Executors.newFixedThreadPool(THREADS);
		//initialize the arrays
		loadUrl = new LoadURL[2];
		storePage = new StorePage[2];
		parsePage = new ParsePage[2];
		downloadImages = new DownloadImages[2];
		modifyPage = new ModifyPage[2];
	}
	
	private void startComponents () throws NamingException, JMSException, InterruptedException {		
		System.out.println("MONITOR: Loading components ...");
		//create the base components
		loadUrl[0] = new LoadURL();
		storePage[0] = new StorePage(aws);
		parsePage[0] = new ParsePage();
		downloadImages[0] = new DownloadImages(aws);
		modifyPage[0] = new ModifyPage();
		
		executor.execute(loadUrl[0]);
		executor.execute(storePage[0]);
		executor.execute(parsePage[0]);
		executor.execute(downloadImages[0]);
		executor.execute(modifyPage[0]);
		
		System.out.println("MONITOR: Components successfully loaded");
		
		try {			
			System.out.println("MONITOR: Start monitoring the queues every " + (TIMEOUT / 1000) + " s"); 
			//create the queues		
			Queue loadUrlQueue = (Queue) initialContext.lookup("LoadURLQueue");
			Queue storePageQueue = (Queue) initialContext.lookup("StorePageQueue");
			Queue parsePageQueue = (Queue) initialContext.lookup("ParsePageQueue");
			Queue downloadImagesQueue = (Queue)initialContext.lookup("DownloadImagesQueue");
			Queue modifyPageQueue = (Queue) initialContext.lookup("ModifyPageQueue");
			//create the queue browsers
			QueueBrowser loadUrlBrowser = jmsContext.createBrowser(loadUrlQueue);
			QueueBrowser storePageBrowser = jmsContext.createBrowser(storePageQueue);
			QueueBrowser parsePageBrowser = jmsContext.createBrowser(parsePageQueue);
			QueueBrowser downloadImagesBrowser = jmsContext.createBrowser(downloadImagesQueue);
			QueueBrowser modifyPageBrowser = jmsContext.createBrowser(modifyPageQueue);
			//start monitoring
			while (true) {
				//check the loadUrl queue
				switch (checkQueueSize(loadUrlBrowser)) {
				case 2:				
					if (loadUrl[1] == null) {
						System.out.println("MONITOR: Create new LoadURL component");
						loadUrl[1] = new LoadURL();
						executor.execute(loadUrl[1]);
					}
					break;
				case 1:
					if (loadUrl[1] != null) {
						System.out.println("MONITOR: Remove one istance of LoadURL");
						loadUrl[1].interrupt();
						loadUrl[1] = null;
					}
					break;
				}
				//check the storePage queue
				switch (checkQueueSize(storePageBrowser)) {
				case 2:
					if (storePage[1] == null) {
						System.out.println("MONITOR: Create new StorePage component");
						storePage[1] = new StorePage(aws);
						executor.execute(storePage[1]);
					}
					break;
				case 1:
					if (storePage[1] != null) {
						System.out.println("MONITOR: Remove one istance of StorePage");
						storePage[1].interrupt();
						storePage[1] = null;
					}
					break;
				}
				//check the parsePage queue
				switch (checkQueueSize(parsePageBrowser)) {
				case 2:
					if (parsePage[1] == null) {
						System.out.println("MONITOR: Create new ParsePage component");
						parsePage[1] = new ParsePage();
						executor.execute(parsePage[1]);
					}
					break;
				case 1:
					if (parsePage[1] != null) {
						System.out.println("MONITOR: Remove one istance of ParsePage");
						parsePage[1].interrupt();
						parsePage[1] = null;
					}
					break;
				}
				//check the downloadPage queue
				switch (checkQueueSize(downloadImagesBrowser)) {
				case 2:
					if (downloadImages[1] == null) {
						System.out.println("MONITOR: Create new DownloadImages component");
						downloadImages[1] = new DownloadImages(aws);
						executor.execute(downloadImages[1]);
					}
					break;
				case 1:
					if (downloadImages[1] != null) {
						System.out.println("MONITOR: Remove one istance of DownloadImages");
						downloadImages[1].interrupt();
						downloadImages[1] = null;
					}
					break;
				}
				//check the modifyPage queue
				switch (checkQueueSize(modifyPageBrowser)) {
				case 2:
					if (modifyPage[1] == null) {
						System.out.println("MONITOR: Create new ModifyPage component");
						modifyPage[1] = new ModifyPage();
						executor.execute(modifyPage[1]);
					}
					break;
				case 1:
					if (modifyPage[1] != null) {
						System.out.println("MONITOR: Remove one istance of ModifyPage");
						modifyPage[1].interrupt();
						modifyPage[1] = null;
					}
					break;
				}
				Thread.sleep(TIMEOUT);
			}	
		} catch (InterruptedException | NamingException | JMSException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Check the number of messages inside a queue
	 * @param browser The QueueBrowser object used to check the specified queue
	 * @return 2 if must be instantiated a new component, 1 if a component can be deleted, 0 if no operation nedeed
	 * @throws JMSException
	 */
	private int checkQueueSize (QueueBrowser browser) throws JMSException {
		@SuppressWarnings("rawtypes")
		Enumeration enumeration = browser.getEnumeration();
		int count = 0;
		while (enumeration.hasMoreElements()) {
			enumeration.nextElement();
			count++;
		}
		if (count >= THRESHOLD) {
			return 2;
		} else if (count == 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
}
