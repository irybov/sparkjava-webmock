package com.github.irybov.sparkwebmock;

import java.io.IOException;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.github.irybov.sparkwebmock.dto.OperationRequest;
import com.google.gson.Gson;

import static spark.Spark.*;

public class WebMockApplication {

	public static void main(String[] args) throws IOException {
		
//		new MessageListener().start();

//		HttpGet request = new HttpGet("http://localhost:8080/bankdemo/bills/operate");
/*		HttpOptions request = new HttpOptions("http://localhost:8080/bankdemo/bills/external");
		request.addHeader("Origin", "http://localhost:4567");
		
        try(CloseableHttpClient httpClient = HttpClients.createDefault();) {
        	
            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 400) {
                    HttpEntity entity = response.getEntity();
                    System.out.println("Response status: " + status);
                    return entity != null ? EntityUtils.toString(entity) : null;
                }
                else {
                	System.out.println("Response status: " + status);
                	throw new ClientProtocolException("Response status: " + status);
                }
            };
        	String response = httpClient.execute(request, responseHandler);
        	System.out.println(response);
        }*/
        
		Set<String> banks = new HashSet<>();
		banks.add("USber");
		banks.add("Gamma");
		banks.add("BTW48");
		banks.add("Penkov");
		
		Set<Currency> currencies = new HashSet<>();
		Currency usd = Currency.getInstance("USD");
		currencies.add(usd);
		Currency eur = Currency.getInstance("EUR");
		currencies.add(eur);
		Currency gbp = Currency.getInstance("GBP");
		currencies.add(gbp);
		Currency rub = Currency.getInstance("RUB");
		currencies.add(rub);
		
		Gson gson = new Gson();
		
        post("/verify", (request, response) -> {
        	
        	OperationRequest operation = gson.fromJson(request.body(), OperationRequest.class);
        	
        	response.type("text/xml");
        	if(banks.contains(operation.getBank())) {
        		if(operation.getRecipient() % 2 == 0) {
        			response.body("Data has been verified");
        			response.status(200);
        			if(!currencies.contains(Currency.getInstance(operation.getCurrency()))) {
            			response.body("Wrong currency type " + operation.getCurrency() + 
            					" for target bill" + operation.getRecipient());
            			response.status(400);
            			throw new HttpResponseException(400, 
            					"Wrong currency type " + operation.getCurrency() + 
            					" for target bill " + operation.getRecipient());
        			}
        		}
        		else {
        			response.body("No bill with serial " + operation.getRecipient() + " found");
        			response.status(404);
        			throw new HttpResponseException(404, 
        					"No bill with serial " + operation.getRecipient() + " found");
        		}
        	}
        	else {
        		response.body("No bank with name " + operation.getBank() + " found");
        		response.status(404);
    			throw new HttpResponseException(404, 
    					"No bank with name " + operation.getBank() + " found");
        	}
        	return response.body();
        	
        });
        
        post("/transfer", (request, response) -> {
        	
        	HttpPost post = new HttpPost("http://localhost:8080/bankdemo/bills/external");
        	
            try(CloseableHttpClient httpClient = HttpClients.createDefault();) {
            	
                ResponseHandler<String> responseHandler = httpResponse -> {                	
                    int status = httpResponse.getStatusLine().getStatusCode();
                    response.status(status);
                    HttpEntity entity = httpResponse.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                };
                HttpEntity string = new StringEntity(request.body(), ContentType.APPLICATION_JSON);
                post.setEntity(string);
                response.body(httpClient.execute(post, responseHandler));        	
            	return response.body();
            }
            
        });
		
        exception(HttpResponseException.class, (exception, request, response) -> {
        	response.type("application/json");
        });
        
	}

}
