package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.NamingException;

import support.Support;


public class Client implements MessageListener {
	
	private static Context initialContext;	
	

	public static void main(String[] args) throws NamingException, IOException {
		
		initialContext = Support.getContext();
		
		// TODO Auto-generated method stub

		String publishQueueName = "LoadURLQueue";
		
		Client listener = new Client();
		
		
		ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("java:comp/DefaultJMSConnectionFactory");
		Queue queue01 = (Queue)initialContext.lookup(publishQueueName);
		
		
		JMSContext jmsContext = cf.createContext();
		Queue queue02 = jmsContext.createTemporaryQueue();
		jmsContext.createConsumer(queue02).setMessageListener(listener);
		JMSProducer producer = jmsContext.createProducer();
		producer.setJMSReplyTo(queue02);
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		
	
		String url = null;
		while (true) {
			System.out.println("CLIENT: Please provide a url (or exit) ->");
			url = bufferedReader.readLine();
			
			if (url.equalsIgnoreCase("exit")) {
				jmsContext.close();
				System.out.println("Goodbye");
				return;
			}
			
			producer.send(queue01, url);
			
		}
		
		
	}
	
	@Override
	public void onMessage(Message msg) {
		// TODO Auto-generated method stub
		
		try {
			String commMessage = msg.getBody(String.class);
			System.out.println("CLIENT: Received -> url(+ " + commMessage+")");
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		if (msg instanceof ObjectMessage) {
			try {
				ObjectMessage objMsg = (ObjectMessage)msg;
				CommunicationMessage commMessage = (CommunicationMessage)objMsg.getObject();
				System.out.println("Received -> username("+commMessage.getUsername()+") - message(+ " + commMessage.getMessage()+")");
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		
	}

}
