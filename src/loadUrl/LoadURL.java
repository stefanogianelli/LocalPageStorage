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


public class LoadURL implements MessageListener {
	
	static JMSContext jmsContext;
	private static Context initialContext;	

	public static void main(String[] args) throws NamingException, IOException {
		
		initialContext = Support.getContext();
		LoadURL server = new LoadURL();
		
		ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("java:comp/DefaultJMSConnectionFactory");
		Queue queueCurr = (Queue)initialContext.lookup("LoadURLQueue");
		
		jmsContext = cf.createContext();
		jmsContext.createConsumer(queueCurr).setMessageListener(server);
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("LoadURL: Waiting for messages...");
		System.out.println("LoadURL: input 'exit' to close");
		
		bufferedReader.readLine();

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
