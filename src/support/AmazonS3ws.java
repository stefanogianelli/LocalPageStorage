package support;

import java.io.File;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;


public class AmazonS3ws {
	
	//private static final String bucketName = "stefanotestamazon";
	private static final String bucketName = "localpagestorageaspoli";
	
	private AmazonS3 s3client;	
	
	public AmazonS3ws() {
		AWSCredentials credentials = null;
		try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
		this.s3client = new AmazonS3Client(credentials);		
	}

    public void uploadS3File(String pathIndexSito){
    	// Es:
    	// String pathSito = "SITI\\www.google.it";
    	try{
    	System.out.println("Inizio salvataggio index file su S3...");
	        s3client.putObject(new PutObjectRequest(bucketName, pathIndexSito+"/index.html", 
	        		new File(pathIndexSito+"/index.html")));
	        System.out.println("Fine salvataggio index file su S3.");		
    	}catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which" +
            		" means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means"+
            		" the client encountered " +
                    "a serious internal problem while trying to " +
                    "communicate with Amazon S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        
    }
	
    public void uploadS3Img(String locPath,String nomeFile, String fileLocalLocation){
    	// Es: 
    	// String fileName = folderName + "testvideo.mp4";
    	// fileLocalLocation = "C:\\Users\\nomeUtente\\Desktop\\nomeImg.jpg"
    	try{
	    	System.out.println("Inizio salvataggio img " + nomeFile + " su S3...");
	        s3client.putObject(new PutObjectRequest(bucketName, locPath+"/images/"+nomeFile, 
	        		new File(fileLocalLocation)));
	        System.out.println("Fine salvataggio img su S3.");
    	}catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which" +
            		" means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means"+
            		" the client encountered " +
                    "a serious internal problem while trying to " +
                    "communicate with Amazon S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
	
	
}
