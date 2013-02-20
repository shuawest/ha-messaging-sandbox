package com.redhat.salab.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MessagingController {
	private static final Logger logger = Logger.getLogger(MessagingController.class.getName());

	private AppSettings settings;
	private MessagingContext lastContext;
	private List<MessagingContext> producerContexts = new ArrayList<MessagingContext>();
	private List<MessagingContext> consumerContexts = new ArrayList<MessagingContext>();
	
	public MessagingController(AppSettings settings) {
		this.settings = settings;
	}
	
	public List<MessagingContext> getProducerContexts() {
		return producerContexts;
	}
	
	public List<MessagingContext> getConsumerContexts() {
		return consumerContexts;
	}
	
	
	public void execute() {		
		createMessagingThreads();
		
		while(!isAllDone()) {
			sleep(1000L);
		}
		
		cleanupAll();
	}
	
	private void createMessagingThreads() {
		try {
			for(int i=0; i < settings.getProducerCount(); i++) {
				MessagingContext producer = createProducer(i);
				producerContexts.add(producer);
			}
			
			for(int i=0; i < settings.getConsumerCount(); i++) {
				MessagingContext consumer = createConsumer(i);
				consumerContexts.add(consumer);
			}
			
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Failed to connect to naming context", e);
			throw new RuntimeException("Failed to connect to naming context");
		} catch (JMSException e) {
			logger.log(Level.SEVERE, "Failed to configure JMS", e);
			throw new RuntimeException("Failed to connect to naming context");
		}
	}
	
	private MessagingContext createProducer(int index) throws NamingException, JMSException {
		MessagingContext context = new MessagingContext();
		context.setContextID("Producer[" + index + ", " + UUID.randomUUID().toString() + "]");
		context.setProducer(true); 

		// Create, or reuse, the Naming Context, and JMS ConnectionFactory / Connection / Destination / Session
		configureJMS(context);				
		
		// Create the producer
		MessageProducer producer = context.getSession().createProducer(context.getDestination());
		context.setMessageProducer(producer);
		
		// Create the producer thread
		MessagingThread producerThread = new MessagingThread(settings, context);
		Thread thread = new Thread(producerThread, context.getContextID());
		thread.start();
		
		lastContext = context;
		return context;
	}
	
	private MessagingContext createConsumer(int index) throws NamingException, JMSException {
		MessagingContext context = new MessagingContext();
		context.setContextID("Consumer[" + index + ", " + UUID.randomUUID().toString() + "]");
		context.setConsumer(true);
		
		// Create, or reuse, the Naming Context, and JMS ConnectionFactory / Connection / Destination / Session
		configureJMS(context);				
		
		// Create the consumer
		MessageConsumer consumer = context.getSession().createConsumer(context.getDestination());
		context.setMessageConsumer(consumer);
		
		// Consumer connections must be started
		context.getConnection().start();
		
		// Create the consumer thread
		MessagingThread consumerThread = new MessagingThread(settings, context);
		Thread thread = new Thread(consumerThread, context.getContextID());
		thread.start();
		
		lastContext = context;
		return context;
	}
		
	private void configureJMS(MessagingContext context) throws NamingException, JMSException {
		// Java Naming Context
		if(settings.isSharingContext() && lastContext != null) {
			context.setNamingContext(lastContext.getNamingContext());
		} else {
			context.setNamingContext(new InitialContext());
		}
		
		// Connection Factory
		if(settings.isSharingConnectionFactory() && lastContext != null) {
			context.setConnectionFactory(lastContext.getConnectionFactory());
		}
		else {
			String cfname = settings.getConnectionFactoryName();
			ConnectionFactory cf = (ConnectionFactory)context.getNamingContext().lookup(cfname);
			context.setConnectionFactory(cf);
		}
		
		// Destination
		if(settings.isSharingDestination() && lastContext != null) {
			context.setDestination(lastContext.getDestination());
		} else {
			String destname = settings.getDestinationName();
			Destination dest = (Destination)context.getNamingContext().lookup(destname);
			context.setDestination(dest);
		}
		
		// Connection
		if(settings.isSharingConnection() && lastContext != null) {
			context.setConnection(lastContext.getConnection());
		}
		else {
			String user = settings.getUsername();
			String pass = settings.getPassword();			
			Connection conn = context.getConnectionFactory().createConnection(user, pass);		
			context.setConnection(conn);			
		}
		
		// Session
		if(settings.isSharingSession() && lastContext != null) {
			context.setSession(lastContext.getSession());
		}
		else {
			Session sess = context.getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
			context.setSession(sess);
		}
	}

	private boolean isAllDone() {
		for(MessagingContext producer : producerContexts)
			if(!producer.isDone())
				return false;
		
		long targetCount = settings.getMessageCount() * settings.getProducerCount() + settings.getConsumeAdditionalCount();
		long consumedCount = 0;
		for(MessagingContext consumer : consumerContexts) {
			consumedCount += consumer.getMessageCount();
		}
		
		if(consumedCount < targetCount)
			return false;
			
		for(MessagingContext consumer : consumerContexts) {
			consumer.setDone(true);
		}
		
		return true;
	}
	
	private void cleanupAll() {
		for(MessagingContext context : producerContexts) 
			cleanup(context);
		for(MessagingContext context : consumerContexts) 
			cleanup(context);
	}
	
	private void cleanup(MessagingContext context)  {
		try {
			logger.log(Level.FINE, "Cleaning up JMS context " + context);
			
			if(context.getMessageProducer() != null)
				context.getMessageProducer().close();
			
			if(context.getMessageConsumer() != null) 
				context.getMessageConsumer().close();
			
			if(context.getSession() != null)
				context.getSession().close();
			
			if(context.getConnection() != null)
				context.getConnection().close();
			
			if(context.getNamingContext() != null)
				context.getNamingContext().close();
			
			logger.log(Level.FINE, "Cleaned up JMS context " + context);
		} catch (JMSException e) {
			logger.log(Level.SEVERE, "Failed to clean up JMS context " + context, e);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Failed to clean up Naming context " + context, e);
		}	
	}
	
	private void sleep(Long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Messaging controller interrupted.", e);
		}
	}
	
}
