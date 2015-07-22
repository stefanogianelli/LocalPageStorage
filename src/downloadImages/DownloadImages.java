package downloadImages;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.NamingException;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import common.MyMessage;
import support.Support;

public class DownloadImages implements MessageListener {
	
	private static JMSContext jmsContext;
	private static Context initialContext;
	
	
	public static void main(String[] args) throws NamingException, IOException {
		
		initialContext = Support.getContext();
		DownloadImages server = new DownloadImages();
		
		ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("java:comp/DefaultJMSConnectionFactory");
		Queue queueCurr = (Queue)initialContext.lookup("DownloadImagesQueue");
		
		jmsContext = cf.createContext();
		jmsContext.createConsumer(queueCurr).setMessageListener(server);
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("DownloadImages: Waiting for url...");
		System.out.println("DownloadImages: input 'exit' to close");
		
		bufferedReader.readLine();
		
	}
	
	@Override
	public void onMessage(Message msg) {		
		try {
			MyMessage message = msg.getBody(MyMessage.class);	
			System.out.println("DownloadImages: Received -> url(" + message.toString() + ") ");
			List<String> images = message.getUrlImage();
			String localPath = message.getPathHtml();
			System.out.println("Starting image download ...");
			List<String> imagePaths = downloadImages(images, localPath);
			System.out.println("Download end");
			
			Queue sendToQueue = (Queue) initialContext.lookup("ModifyPageQueue");
			jmsContext.createProducer().send(sendToQueue, new MyMessage(message.getUrlHtml(),
					message.getPathHtml(),
					images,
					imagePaths));
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private static List<String> downloadImages(List<String> images, String locPath){
		List<String> imagePaths = new ArrayList<String>();
		locPath = locPath.replace("\\index.html", "");
		int count = 0;
		for (String image : images) {
			//Open a URL Stream
			Response resultImageResponse;
			try {
				resultImageResponse = Jsoup.connect(image).ignoreContentType(true).execute();
				String nomeFile = image.substring(image.lastIndexOf("/") + 1);
				String destPath = locPath + "\\images\\" + count++ + "_" + nomeFile;
				File filePath = new File(destPath);
				filePath.getParentFile().mkdirs();
				// output here
				FileOutputStream out = (new FileOutputStream(filePath));
				out.write(resultImageResponse.bodyAsBytes());  // resultImageResponse.body() is where the image's contents are.
				out.close();
				imagePaths.add(destPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return imagePaths;
		
	}
	
	

}
