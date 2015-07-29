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
import support.AmazonS3ws;
import support.Support;

public class DownloadImages extends Thread implements MessageListener {	
	private JMSContext jmsContext;
	private Context initialContext;
	private AmazonS3ws myAWS;
	
	@Override
	public void run () {
		try {
			myAWS = new AmazonS3ws();
			
			initialContext = Support.getContext();
			
			ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("java:comp/DefaultJMSConnectionFactory");
			Queue queueCurr = (Queue)initialContext.lookup("DownloadImagesQueue");
			
			jmsContext = cf.createContext();
			jmsContext.createConsumer(queueCurr).setMessageListener(this);
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("DownloadImages: Waiting for url...");
			System.out.println("DownloadImages: input 'exit' to close");
			
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
			System.out.println("DownloadImages: Received -> url(" + message.toString() + ") ");
			List<String> images = message.getUrlImage();
			String localPath = message.getPathHtml();
			System.out.println("Starting image download ...");
			List<String> imagePaths = downloadImages(images, localPath);
			System.out.println("Download end");
			
			Queue sendToQueue = (Queue) initialContext.lookup("ModifyPageQueue");
			// Invio alla coda
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
	
	private List<String> downloadImages(List<String> images, String locPath){
		List<String> imagePaths = new ArrayList<String>();
		locPath = locPath.replace("\\index.html", "");
		int count = 0;
		//AmazonS3ws myAWS = new AmazonS3ws();
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
				// Salvataggio delle immagini su S3
				myAWS.uploadS3Img(locPath,nomeFile,destPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return imagePaths;
		
	}

}
