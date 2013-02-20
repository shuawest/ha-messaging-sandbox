package com.redhat.salab.messaging;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;

public class MessagingContext {
	private static final String MESSAGE_DELIMITER = "\n";
	
    private Context namingContext;
	private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private MessageConsumer consumer;
    private Destination destination;
    
    private boolean isProducer = false;
    private boolean isConsumer = false;
    private boolean isDone = false;
    private boolean isPaused = false;
    private String contextID;
    private long messageCount = 0;
    private String messages = "";

    
	public Context getNamingContext() {
		return namingContext;
	}
	public void setNamingContext(Context context) {
		this.namingContext = context;
	}
	
	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
	
	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public Session getSession() {
		return session;
	}
	public void setSession(Session session) {
		this.session = session;
	}
	
	public MessageProducer getMessageProducer() {
		return producer;
	}
	public void setMessageProducer(MessageProducer producer) {
		this.producer = producer;
	}
	
	public MessageConsumer getMessageConsumer() {
		return consumer;
	}
	public void setMessageConsumer(MessageConsumer consumer) {
		this.consumer = consumer;
	}
	
	public Destination getDestination() {
		return destination;
	}
	public void setDestination(Destination destination) {
		this.destination = destination;
	}
	
	public boolean isProducer() {
		return isProducer;
	}
	public void setProducer(boolean isProducer) {
		this.isProducer = isProducer;
		this.isConsumer = !isProducer;
	}
	
	public boolean isConsumer() {
		return isConsumer;
	}
	public void setConsumer(boolean isConsumer) {
		this.isConsumer = isConsumer;
		this.isProducer = !isConsumer;
	}
	
	public boolean isDone() {
		return isDone;
	}
	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}
	
	public boolean isPaused() {
		return isPaused;
	}
	public void setPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}
	
	public String getContextID() {
		return contextID;
	}
	public void setContextID(String contextID) {
		this.contextID = contextID;
	}
	
	public long getMessageCount() {
		return messageCount;
	}
	public void setMessageCount(long messageCount) {
		this.messageCount = messageCount;
	}
	public void incrementMessageCount() {
		this.messageCount++;
	}
	
	public String getMessages() {
		return messages;
	}
	public void setMessages(String messages) {
		this.messages = messages;
	}
	public void appendMessage(String msg) {
		this.messages = messages + msg + MESSAGE_DELIMITER;
	}    
}
