package storePage;

import java.io.File;
import java.io.IOException;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.NamingException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import common.MyMessage;

import java.io.*;

import support.AmazonS3ws;
import support.Support;

public class StorePage extends Thread implements MessageListener {	
	private JMSContext jmsContext;
	private Context initialContext;
	private AmazonS3ws myAWS;
	
	@Override
	public void run () {
		try {
			myAWS = new AmazonS3ws();

			initialContext = Support.getContext();
			
			ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("java:comp/DefaultJMSConnectionFactory");
			Queue queueCurr = (Queue)initialContext.lookup("StorePageQueue");
			
			jmsContext = cf.createContext();
			jmsContext.createConsumer(queueCurr).setMessageListener(this);
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("StorePage: Waiting for url...");
			System.out.println("StorePage: input 'exit' to close");
			
			bufferedReader.readLine();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onMessage(Message msg) {		
		try {
			MyMessage message = msg.getBody(MyMessage.class);
			System.out.println("StorePage: Received -> url(" + message.toString() + ") ");
			System.out.println("Starting page storing ...");
			String path = storePage(message.getUrlHtml());	
			System.out.println("Page stored successfully");
						
			Queue sendToQueue = (Queue) initialContext.lookup("ParsePageQueue");

			// Invio alla coda
			jmsContext.createProducer().send(sendToQueue, new MyMessage(message.getUrlHtml(), path));
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private String storePage (String url) {
		Document doc = null;
		String newPath = getNewPath(url);
		try {
			doc = Jsoup.connect(url).get();
			Elements tags = doc.select("[src]");
			for (Element e : tags) {
				String path = e.attr("abs:src");
				e.attr("src", path);
			}
			
			Elements refs = doc.select("[href]");
			for (Element e : refs) {
				String path = e.attr("abs:href");
				e.attr("href", path);
			}
			
			File file = new File(newPath);				
			file.getParentFile().mkdirs();
			file.createNewFile();
			
			FileOutputStream fop = new FileOutputStream(file);
 
			// get the content in bytes
			byte[] contentInBytes = doc.html().getBytes();
 
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
			
			// Salvataggio della directory su S3
			myAWS.uploadS3File(newPath.replace("\\index.html",""));		
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return newPath;
	}
	
	private String getNewPath(String oldPath) {
		String path = "SITI\\"+oldPath.replace("http://","").replace("/","\\")+"\\index.html";
		path = path.replace("?", "");
		return path;
	}
	

}