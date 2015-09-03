package support;

import java.io.File;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.transfer.TransferManager;

public class AmazonS3ws {
	
	//private static final String bucketName = "stefanotestamazon";
	private static final String bucketName = "localpagestorageaspoli";
	
	private TransferManager tx;
	
	public AmazonS3ws() {
		DefaultAWSCredentialsProviderChain credentialProviderChain = new DefaultAWSCredentialsProviderChain();
		tx = new TransferManager(credentialProviderChain.getCredentials());
	}
	
	public void uploadS3File (String pathIndexSito) {
		//System.out.println("Salvataggio index file su S3...");
		tx.upload(bucketName, pathIndexSito.replace("SITI\\", "") + "/index.html", new File(pathIndexSito+"/index.html"));
	}
	
	public void uploadS3Img(String locPath,String nomeFile, String fileLocalLocation){
		//System.out.println("Salvataggio immagine " + nomeFile + " su S3...");
		tx.upload(bucketName, locPath.replace("SITI\\", "") + "/images/" + nomeFile, new File(fileLocalLocation));
	}	
	
	public void uploadS3Folder (String path) {
		//System.out.println("Salvataggio cartella immagini su S3...");
		tx.uploadDirectory(bucketName, path, new File(path), true);
	}
	
}
