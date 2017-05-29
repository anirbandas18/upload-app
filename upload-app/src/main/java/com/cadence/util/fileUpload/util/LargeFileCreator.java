package com.cadence.util.fileUpload.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LargeFileCreator {

	private static final Long FILE_LENGTH = 1024 * 1024 * 1024 * 5L;
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		for(Integer i = 1 ; i <= 5 ; i++) {
			String fileName = i.toString();
			String filePath = args[0] + File.separator + fileName;
			String mode= "rw";
			RandomAccessFile raf = new RandomAccessFile(filePath, mode);
			raf.setLength(FILE_LENGTH);
			raf.close();
		}
	}

}
