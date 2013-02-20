package com.redhat.salab.messaging;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AppSettings {
	private static final Logger log = LoggerFactory.getLogger(AppSettings.class);
	
	public static final String CONNECTION_FACTORY = "connection.factory";
	public static final String DESTINATION_NAME = "destination.name";
	public static final String SHARE_CONTEXT = "share.context";
	public static final String SHARE_DESTINATION = "share.destination";
	public static final String SHARE_FACTORY = "share.connection.factory";
	public static final String SHARE_CONNECTION = "share.connection";
	public static final String SHARE_SESSION = "share.session";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String MESSAGE_COUNT = "message.count";
	public static final String MESSAGE_DELAY = "message.delay";  // in milliseconds
	public static final String PRODUCER_COUNT = "producer.count";
	public static final String CONSUMER_COUNT = "consumer.count";
	public static final String CONSUME_ADDITIONAL = "consume.additional";
	
	public String getConnectionFactoryName() {
		return getStringProperty(CONNECTION_FACTORY);
	}
	
	public String getDestinationName() {
		return getStringProperty(DESTINATION_NAME);
	}

	public boolean isSharingContext() {
		return getBooleanProperty(SHARE_CONTEXT);
	}
	public boolean isSharingConnectionFactory() {
		return getBooleanProperty(SHARE_FACTORY);
	}
	public boolean isSharingDestination() {
		return getBooleanProperty(SHARE_DESTINATION);
	}
	public boolean isSharingConnection() {
		return getBooleanProperty(SHARE_CONNECTION);
	}
	public boolean isSharingSession() {
		return getBooleanProperty(SHARE_SESSION);
	}
	
	public String getUsername() {
		return getStringProperty(USERNAME);
	}
	
	public String getPassword() {
		return getStringProperty(PASSWORD);
	}
	
	public long getMessageCount() {
		return getLongProperty(MESSAGE_COUNT);
	}
	public long getConsumeAdditionalCount() {
		return getLongProperty(CONSUME_ADDITIONAL);
	}
	
	public boolean isInfiniteMessageCount() {
		return (-1 == getMessageCount());
	}

	public long getMessageDelayMs() {
		return getLongProperty(MESSAGE_DELAY);
	}
	
	public int getProducerCount() {
		return getIntegerProperty(PRODUCER_COUNT);
	}
	
	public int getConsumerCount() {
		return getIntegerProperty(CONSUMER_COUNT);
	}
	
	//
	// Generic Property Processors

	public long getLongProperty(String propName) { 
		return Long.parseLong(getStringProperty(propName));
	}
	
	public int getIntegerProperty(String propName) { 
		return Integer.parseInt(getStringProperty(propName));
	}
	
	public boolean getBooleanProperty(String propName) { 
		return Boolean.parseBoolean(getStringProperty(propName));
	}
	
	public String getStringProperty(String propName) {
		return System.getProperty(propName);
	}
	
	//
	// Helper Methods
	
	public void loadPropertiesFromFile(String filepath) {		
		try{  
			Properties props = new Properties();
	        FileInputStream inputStream = new FileInputStream(filepath);  
	        props.load(inputStream);  
			overlaySystemProperties(props);
	    } catch(IOException e){  
			log.error("Properties file '{}' failed to load", filepath); 
	    } 
	}
	
	public void loadPropertiesFromClasspath(String classpath) {
		try {
			Properties props = new Properties();
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(classpath);
			props.load(inputStream);
			overlaySystemProperties(props);
	    } catch(IOException e){  
			log.error("Properties file '{}' failed to load from classpath",classpath); 
	    } 
	}
	
	public void overlaySystemProperties(Properties properties) {		
		for(String key : properties.stringPropertyNames()) {
			System.setProperty(key, properties.getProperty(key));
		}
	}
	
}
