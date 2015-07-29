package modifyPage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

public class ModifyPage extends Thread implements MessageListener {
	
	private JMSContext jmsContext;
	private Context initialContext;
	
	@Override
	public void run () {
		try {
			initialContext = Support.getContext();
			
			ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("java:comp/DefaultJMSConnectionFactory");
			Queue queueCurr = (Queue)initialContext.lookup("ModifyPageQueue");
			
			jmsContext = cf.createContext();
			jmsContext.createConsumer(queueCurr).setMessageListener(this);
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("ModifyPage: Waiting for url...");
			System.out.println("ModifyPage: input 'exit' to close");
			
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
			System.out.println("ModifyPage: Received -> url(" + message.toString() + ") ");
			System.out.println("Starting modify page ...");
			modifyPage(message.getPathHtml(), message.getUrlImage(), message.getPathImage());
			System.out.println("Page modified successfully");
		} catch (JMSException e) {
			e.printStackTrace();
		}	
	}	
	
	private void modifyPage (String filePath, List<String> originalUrl, List<String> localUrl) {
		Path path = Paths.get(filePath);
		String content;
		try {
			content = new String(Files.readAllBytes(path));
			for (int i = 0; i < localUrl.size(); i++) {
				String imageLocalUrl = localUrl.get(i).replace("\\", "\\\\");
				imageLocalUrl = ".\\\\" + imageLocalUrl.substring(imageLocalUrl.indexOf("images", 0));
				System.out.println(i + ") Original URL: " + originalUrl.get(i));
				System.out.println(i + ") Local URL: " + imageLocalUrl);
				content = content.replaceAll(originalUrl.get(i), imageLocalUrl);
			}			
			Files.write(path, content.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
