package com.cadence.util.fileUpload.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CheckSum {
	
	private String filePath;
	
	public CheckSum(String filePath) {
		this.filePath = filePath;
	}
	
	public String generate() throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		FileInputStream fis = new FileInputStream(filePath);
		byte[] dataBytes = new byte[1024];
		int nread = 0;
		while ((nread = fis.read(dataBytes)) != -1) {
			md.update(dataBytes, 0, nread);
		};
		fis.close();
		byte[] mdbytes = md.digest();
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	public static void main(String args[]) throws Exception {
		File clientDir = new File(args[0]);
		File serverDir = new File(args[1]);
		File clientFiles[] = clientDir.listFiles();
		File serverFiles[] = serverDir.listFiles();
		if(clientFiles.length == serverFiles.length) {
			int noOfFiles = clientFiles.length;
			for(int i = 0 ; i < noOfFiles ; i++) {
				String clientFilePath = clientFiles[i].getAbsolutePath();
				String serverFilePath = serverFiles[i].getAbsolutePath();
				CheckSum client = new CheckSum(clientFilePath);
				CheckSum server = new CheckSum(serverFilePath);
				String clientMD5 = client.generate();
				String serverMD5 = server.generate();
				System.out.println(clientFilePath + " " + clientMD5.equals(serverMD5) + " " + serverFilePath);
			}
		}
	}
}