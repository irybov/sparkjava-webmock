package com.github.irybov.sparkwebmock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class MessageListener extends Thread {
	
	private Properties props;
	private String exchange;
	private String queue;
	
	{
		props = new Properties();
		Path path = FileSystems.getDefault().getPath("rabbit.properties").toAbsolutePath();
		
		if(Files.exists(path) && Files.isReadable(path)) {
			try {props.load(Files.newBufferedReader(path));}
			catch (IOException exc) {exc.printStackTrace();}
		}
		else {
	        try (InputStream input = WebMockApplication.class
	        		.getClassLoader()
	        		.getResourceAsStream("rabbit.properties")) {
	        	props.load(input);
	        }
	        catch (IOException exc) {exc.printStackTrace();}
		}
	}
	
	public void run() {
		
		exchange = props.getProperty("exchange");
		queue = exchange.concat(".").concat(props.getProperty("group"));
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(props.getProperty("host"));
		factory.setPort(Integer.parseInt(props.getProperty("port")));
		factory.setUsername(props.getProperty("username"));
		factory.setPassword(props.getProperty("password"));
		Connection connection;
		Channel channel = null;
//		String queue = null;
		
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true, false, null);
//			channel.exchangeDeclarePassive(exchange);
			channel.queueDeclare(queue, true, false, false, null);
//			queue = channel.queueDeclare().getQueue();
			channel.queueBind(queue, exchange, "T");
		}
		catch (IOException | TimeoutException exc) {exc.printStackTrace();}

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
           String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
           System.out.println(message);
        };
        
        try {channel.basicConsume(queue, true, deliverCallback, consumerTag -> {});}
        catch (IOException exc) {exc.printStackTrace();}
	}
}
