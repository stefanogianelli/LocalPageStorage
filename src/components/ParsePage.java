package components;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import support.Support;

public class ParsePage extends Thread implements MessageListener {
	
	private JMSContext jmsContext;
	private Context initialContext;
	private List<String> validImagesFormat = new ArrayList<>(Arrays.asList("jpg", "jpeg", "gif", "png"));
	
	@Override
	public void run () {
		try {
			initialContext = Support.getContext();
			
			ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("java:comp/DefaultJMSConnectionFactory");
			Queue queueCurr = (Queue)initialContext.lookup("ParsePageQueue");
			
			jmsContext = cf.createContext();
			jmsContext.createConsumer(queueCurr).setMessageListener(this);
			
			System.out.println("[TID: " + this.getId() + "] ParsePage: Waiting for url...");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onMessage(Message msg) {		
		try {
			MyMessage message = msg.getBody(MyMessage.class);	
			System.out.println("[TID: " + this.getId() + "] ParsePage: Received -> url(" + message.toString() + ") ");
			System.out.println("[TID: " + this.getId() + "] Starting parse ...");
			List<String> images = parsePage(message.getUrlHtml());
			System.out.println("[TID: " + this.getId() + "] Parsing end");
			
			Queue sendToQueue = (Queue) initialContext.lookup("DownloadImagesQueue");
			// Invio alla coda
			jmsContext.createProducer().send(sendToQueue, new MyMessage(message.getUrlHtml(),
					message.getPathHtml(),
					images));
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	private List<String> parsePage(String url){
		List<String> images = new ArrayList<String>();
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
			Elements tags = doc.select("[src]");
			for (Element e : tags) {
				if (e.tagName().equals("img")) {
					String path = e.attr("abs:src");
					if (path.lastIndexOf("?") != -1) {
						path = path.substring(0, path.lastIndexOf("?"));
					}
					//System.out.println("Found image: " + path);
					String ext = path.substring(path.lastIndexOf(".") + 1);
					if (!images.contains(path) && validImagesFormat.contains(ext.toLowerCase())) {
						images.add(path);
						//System.out.println("--> Immagine Aggiunta");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return images;
	}
	
	
	
	
	
	
	
	
	
	
	

}
