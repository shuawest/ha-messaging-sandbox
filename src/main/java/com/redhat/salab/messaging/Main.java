package com.redhat.salab.messaging;

import java.util.logging.Logger;


public class Main {
	private static final Logger logger = Logger.getLogger(Main.class.getName());

	public static void main(String[] argv) {
		AppSettings settings = new AppSettings();
		settings.loadPropertiesFromClasspath("default.properties");
		for(String arg : argv) {
			settings.loadPropertiesFromFile(arg);
		}
			
		long totalCount = settings.getMessageCount() * settings.getProducerCount();
		
		logger.info("Starting messaging test...");
		logger.info("- Producing " + totalCount + " messages with " + settings.getProducerCount() + " producers.");
		logger.info("- Consuming messages with " + settings.getConsumerCount() + " consumers.");

		MessagingController controller = new MessagingController(settings);
		controller.execute();
		
		for(MessagingContext context : controller.getConsumerContexts()) {
			logger.info("Consumed " + context.getMessageCount() + 
					" messages on consumer '" + context.getContextID() + 
					"':\n" + context.getMessages());
		}
		
		logger.info("Complete.");
	}
}
