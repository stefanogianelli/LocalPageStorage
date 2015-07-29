package loadUrl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.NamingException;

import common.MyMessage;

import support.Support;

public class LoadURL extends Thread implements MessageListener {
	
	private JMSContext jmsContext;
	private Context initialContext;
	
	@Override
	public void run () {
		try {
			initialContext = Support.getContext();
			
			ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("java:comp/DefaultJMSConnectionFactory");
			Queue queueCurr = (Queue)initialContext.lookup("LoadURLQueue");
			
			jmsContext = cf.createContext();
			jmsContext.createConsumer(queueCurr).setMessageListener(this);
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("LoadURL: Waiting for messages...");
			System.out.println("LoadURL: input 'exit' to close");
			
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
			String url = msg.getBody(String.class);
			System.out.println("LoadURL: Received -> url( " + url+") ");
			
			Queue replyToQueue = (Queue) msg.getJMSReplyTo();
			jmsContext.createProducer().send(replyToQueue, url);
			
			Queue sendToQueue = (Queue)initialContext.lookup("StorePageQueue");
			// Invio alla coda
			jmsContext.createProducer().send(sendToQueue, new MyMessage(url));
			
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}
		
		
	}

}
