package com.redhat.salab.messaging;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.TextMessage;


public class MessagingThread implements Runnable {
	private static final Logger logger = Logger.getLogger(MessagingThread.class.getName());
	
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
					logger.log(Level.FINE, this+" paused, waiting to be unpaused...");
					continue;
				}
				
				if(context.isProducer()) 
					produce();
				else 
					consume();
				
				Thread.sleep(settings.getMessageDelayMs());			
			} catch (JMSException jmsEx) {
				logger.log(Level.SEVERE, context+" received a JMS error", jmsEx);
				return;
			} catch (InterruptedException threadEx) {
				logger.log(Level.SEVERE, context+" interrupted", threadEx);	
				return;
			} 
		}
		logger.log(Level.INFO, "Thread is done");
	}
	
	private void produce() throws JMSException {
		context.incrementMessageCount();
		
		String msg = String.format("Message %5d from %s", context.getMessageCount(), context.getContextID());
		TextMessage jmsMessage = context.getSession().createTextMessage(msg);							
		context.getMessageProducer().send(jmsMessage); 
		context.appendMessage(msg);	
		
		logger.log(Level.FINE, context.getContextID()+" produced message '"+msg+"'");
		
		if(context.getMessageCount() == settings.getMessageCount()) {
			context.setDone(true);
			logger.log(Level.FINE, context.getContextID()+" done producing messages");
		}
	}
	
	private void consume() throws JMSException {
		TextMessage jmsMessage = (TextMessage)context.getMessageConsumer().receiveNoWait();
		if(jmsMessage != null) {
			String msg = jmsMessage.getText();			
			
			logger.log(Level.FINE, context+" consumed message '"+msg+"'");
			
			context.appendMessage(msg);	
			context.incrementMessageCount();	
		}
	}
}
