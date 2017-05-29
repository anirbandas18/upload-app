package com.cadence.util.fileUpload.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Config {

	private static Properties applicationProperties;

	static {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream input = classLoader.getResourceAsStream("/com/cadence/util/fileUpload/resources/application.properties");
			applicationProperties = new Properties();
			applicationProperties.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}
	}
	public static String getValue(Constants key) {
		return applicationProperties.getProperty(key.toString());
	}
	
}
