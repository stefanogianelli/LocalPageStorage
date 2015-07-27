package support;

import java.util.Enumeration;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Support {

	public static Context getContext() throws NamingException {
		Properties props = new Properties();
		props.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
		props.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
		props.setProperty("java.naming.provider.url", "iiop://localhost:3700");
		return new InitialContext(props);
	}
	
	public static boolean checkQueueOverload(String queueName, int threshold)  throws NamingException, JMSException {
		
		InitialContext initialContext = (InitialContext) Support.getContext();
		ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("java:comp/DefaultJMSConnectionFactory");

		Queue queue = (Queue) initialContext.lookup(queueName);
		JMSContext jmsContext = cf.createContext(); //

		QueueConnection queueConn = (QueueConnection) cf.createConnection();
		QueueSession queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		QueueBrowser queueBrowser = queueSession.createBrowser(queue);
		queueConn.start();
		Enumeration e = queueBrowser.getEnumeration();
        int numMsgs = 0;
        // count number of messages
        while (e.hasMoreElements()) {
            Message message = (Message) e.nextElement();
            numMsgs++;
        }
        
        if(numMsgs >= threshold){
        	return true;
        }else{
        	return false;
        }
	}
	
}
