package util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.kms.model.ListKeysRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3Uploader {
	private static String bucketName = "wglobe";
	private static String keyName;

	// ~/.aws/credentials
	BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAJ4L6POF6C5HA4AQQ", "wmHk8K6I9qIiJKQyxCUikO++7TU6w4fTyKDZXonO");
	AmazonS3 s3client = new AmazonS3Client(awsCreds);

	public String putObjectwithInputStream(InputStream is, String filename) {
		String url = "";
		keyName = "netcdf/";
		try {
			ObjectMetadata objdata = new ObjectMetadata();
			s3client.setEndpoint("http://s3.amazonaws.com");
			s3client.putObject(new PutObjectRequest(bucketName, keyName
					+ filename, is, objdata)
					.withCannedAcl(CannedAccessControlList.PublicRead));

			url = "http://s3-us-west-2.amazonaws.com/wglobe/" + keyName
					+ filename;
		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which "
					+ "means your request made it "
					+ "to Amazon S3, but was rejected with an error response"
					+ " for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which "
					+ "means the client encountered "
					+ "an internal error while trying to "
					+ "communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}

		return url;
	}

	public void putObjectwithFileList(String key, ArrayList<File> f) {

		String url = "";
		keyName = "netcdfImages/" + key + "/";
		
		java.util.Date expiration = new java.util.Date();
		long msec = expiration.getTime();
		msec += 1000 * 60 * 60; // 1 hour.
		expiration.setTime(msec);

		try {

			for (int i = 0; i < f.size(); i++) {
				s3client.putObject(new PutObjectRequest(bucketName, keyName
						+ f.get(i).getName(), f.get(i))
						.withCannedAcl(CannedAccessControlList.PublicRead));
//				url = "http://s3-us-west-2.amazonaws.com/wglobe/" + keyName
//						+ f.get(i).getName();
			
				System.out.println("Uploading " + keyName + f.get(i).getName());
			}
		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which "
					+ "means your request made it "
					+ "to Amazon S3, but was rejected with an error response"
					+ " for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which "
					+ "means the client encountered "
					+ "an internal error while trying to "
					+ "communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}

	}

	public boolean checkKeyExistbyVariable(String variable) {
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(bucketName).withPrefix("netcdfImages/" + variable);
		ObjectListing objectListing;
		do {
			objectListing = s3client.listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : objectListing
					.getObjectSummaries()) {
				System.out.println(" - " + objectSummary.getKey() + "  "
						+ "(size = " + objectSummary.getSize() + ")");
				return true;
			}
		} while (objectListing.isTruncated());
		return false;
	}

	public ArrayList<String> getFileListbyVariable(String variable) {
		ArrayList<String> fl = new ArrayList<String>();
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(bucketName).withPrefix(variable + "/")
				.withDelimiter("/");
		ObjectListing objectListing;
		do {
			objectListing = s3client.listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : objectListing
					.getObjectSummaries()) {
				String filename = objectSummary.getKey();
				if (filename.endsWith("png")) {
					fl.add("http://s3-us-west-2.amazonaws.com/wglobe/"
							+ filename);
				}

			}
		} while (objectListing.isTruncated());

		return fl;
	}
}
