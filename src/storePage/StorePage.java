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

import support.Support;

public class StorePage implements MessageListener {
	
	private static JMSContext jmsContext;
	private static Context initialContext;	

	public static void main(String[] args) throws NamingException, IOException {
		
		initialContext = Support.getContext();
		StorePage server = new StorePage();
		
		ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("java:comp/DefaultJMSConnectionFactory");
		Queue queueCurr = (Queue)initialContext.lookup("StorePageQueue");
		
		jmsContext = cf.createContext();
		jmsContext.createConsumer(queueCurr).setMessageListener(server);
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("StorePage: Waiting for url...");
		System.out.println("StorePage: input 'exit' to close");
		
		bufferedReader.readLine();
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
