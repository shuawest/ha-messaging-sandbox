package com.redhat.salab.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;


public class MessagingThread implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(MessagingThread.class);
	
	private AppSettings settings;
	private MessagingContext context;
	
	public MessagingThread(AppSettings settings, MessagingContext context) {
		this.settings = settings;
		this.context = context;
	}

	@Override
	public void run() {			
		while(!context.isDone()) {
			try {
				if(context.isPaused()) {
					log.trace("Paused, waiting to be unpaused...");
					continue;
				}
				
				if(context.isProducer()) 
					produce();
				else 
					consume();
				
				Thread.sleep(settings.getMessageDelayMs());			
			} catch (JMSException jmsEx) {
				log.error("Received a JMS error", jmsEx);
				return;
			} catch (InterruptedException threadEx) {
				log.error("Interrupted", threadEx);	
				return;
			} 
		}
		log.trace("Thread shutting down.");
	}
	
	private void produce() throws JMSException {
		context.incrementMessageCount();
		
		String msg = String.format("Message %5d from %s", context.getMessageCount(), context.getContextID());
		TextMessage jmsMessage = context.getSession().createTextMessage(msg);
		context.getMessageProducer().send(jmsMessage); 		
		context.appendMessage(msg);	
		
		log.debug("Produced message '{}'", msg);
		
		if(context.getMessageCount() == settings.getMessageCount()) {
			context.setDone(true);
			log.info("{} done producing messages.", context);
		}
	}
	
	private void consume() throws JMSException {
		TextMessage jmsMessage = (TextMessage)context.getMessageConsumer().receiveNoWait();			
		
		if(jmsMessage != null) {
			String msg = jmsMessage.getText();			
			
			log.debug("Consumed message '{}'", msg);
			
			context.appendMessage(msg);	
			context.incrementMessageCount();	
		}
	}
}
