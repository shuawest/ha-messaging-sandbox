package com.redhat.salab.messaging;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] cliArguments) {
		AppSettings settings = processProperties(cliArguments);
		
		long totalCount = settings.getMessageCount() * settings.getProducerCount();
		
		log.info("Starting messaging test...");
		log.info("- Producing {} messages with {} producers.", totalCount, settings.getProducerCount());
		log.info("- Consuming messages with {} consumers.", settings.getConsumerCount());

		MessagingController controller = new MessagingController(settings);
		controller.execute();
		
		for(MessagingContext context : controller.getConsumerContexts()) {
			log.info("Consumed {} messages on consumer '{}':\n{}", context.getMessageCount(), context.getContextID(), context.getMessages());
		}
		
		log.info("Complete.");
	}
	
	private static AppSettings processProperties(String[] cliArguments) {
		AppSettings settings = new AppSettings();
		
		// Backup current system properties
		Properties sysprops = System.getProperties();
		
		// Start by setting properties defined in system properties
		settings.loadPropertiesFromClasspath("default.properties");
		
		// Load property file referenced command line - if specified
		for(String arg : cliArguments) {
			settings.loadPropertiesFromFile(arg);
		}
		
		// Overlay system properties on top so that java -Dprop=value may be specified
		settings.overlaySystemProperties(sysprops);
		
		return settings;
	}
}
