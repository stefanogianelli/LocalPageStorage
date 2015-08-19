package components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
	
	public DownloadImages (AmazonS3ws aws) {
		myAWS = aws;
	}
	
	@Override
	public void run () {
		try {			
			initialContext = Support.getContext();
			
			ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("java:comp/DefaultJMSConnectionFactory");
			Queue queueCurr = (Queue)initialContext.lookup("DownloadImagesQueue");
			
			jmsContext = cf.createContext();
			jmsContext.createConsumer(queueCurr).setMessageListener(this);
			
			System.out.println("[TID: " + this.getId() + "] DownloadImages: Waiting for url...");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onMessage(Message msg) {		
		try {
			MyMessage message = msg.getBody(MyMessage.class);	
			System.out.println("[TID: " + this.getId() + "] DownloadImages: Received -> url(" + message.toString() + ") ");
			List<String> images = message.getUrlImage();
			String localPath = message.getPathHtml();
			System.out.println("[TID: " + this.getId() + "] Starting image download ...");
			List<String> imagePaths = downloadImages(images, localPath);
			System.out.println("[TID: " + this.getId() + "] Download end");
			
			Queue sendToQueue = (Queue) initialContext.lookup("ModifyPageQueue");
			// Invio alla coda
			jmsContext.createProducer().send(sendToQueue, new MyMessage(message.getUrlHtml(),
					message.getPathHtml(),
					images,
					imagePaths));
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	private List<String> downloadImages(List<String> images, String locPath){
		List<String> imagePaths = new ArrayList<String>();
		locPath = locPath.replace("\\index.html", "");
		int count = 0;
		for (String image : images) {
			//Open a URL Stream
			Response resultImageResponse;
			try {
				resultImageResponse = Jsoup.connect(image).ignoreContentType(true).execute();
				String nomeFile = image.substring(image.lastIndexOf("/") + 1);
				String nomeFileUnivoco = count++ + "_" + nomeFile;
				String destPath = locPath + "\\images\\" + nomeFileUnivoco;
				File filePath = new File(destPath);
				filePath.getParentFile().mkdirs();
				// output here
				FileOutputStream out = (new FileOutputStream(filePath));
				out.write(resultImageResponse.bodyAsBytes());  // resultImageResponse.body() is where the image's contents are.
				out.close();
				imagePaths.add(destPath);
				// Salvataggio delle immagini su S3
				myAWS.uploadS3Img(locPath,nomeFileUnivoco,destPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//Save images folder on Amazon S3
		//myAWS.uploadS3Folder(locPath + "\\images");
		return imagePaths;
		
	}

}
