package com.endurata.spotracer;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class WSAssistant {

	private String url;
	private List<NameValuePair> nameValuePair;
	private HttpClient httpClient;
	private HttpPost httpPost;
	
	public WSAssistant() {
		init();
	}
	
	public WSAssistant(String url) {
		init();
		setUrl(url);
	}
	
	private void init() {
		nameValuePair = new ArrayList<NameValuePair>(2);
		httpClient = new DefaultHttpClient();
	}
	
	public String invokeService() {
		String sResponse = "No response";
		// Url encoding the POST parameters
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
		} catch (UnsupportedEncodingException e) {
			// Writing error to log
			e.printStackTrace();
		}
		// Make the HTTP request
		try {
			HttpResponse response = httpClient.execute(httpPost);
			sResponse = response.toString();
			// Write response to log
			Log.d("WS", "Http Response: " + sResponse);
			// Get content from response
			HttpEntity he = response.getEntity();
			sResponse = parseEntityResponse(EntityUtils.toString(he));
		} catch (ClientProtocolException e) {
			// Write exception to log
			e.printStackTrace();
		} catch (IOException e) {
			// Write exception to log
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sResponse;
	}
	
	private String parseEntityResponse(String entityResponse) {
		String[] parts;
		String delim1 = "<ns:return>";
		String delim2 = "</ns:return>";
		String sMessage = "";
		if (entityResponse.contains(delim1)) {
			parts = entityResponse.split(delim1);
			entityResponse = parts[1];
		}
		if (entityResponse.contains(delim2)) {
			parts = entityResponse.split(delim2);
			entityResponse = parts[0];
			sMessage = entityResponse;
		}
		return sMessage;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
		Log.d("WS", "url to set: " + url);
		httpPost = new HttpPost(url);
	}

	/**
	 * Building post parameters.
	 * @param name  - url post parameter name
	 * @param value - url post parameter value
	 */
	public void setParameter(String name, String value) {
		// Name and value pair
		nameValuePair.add(new BasicNameValuePair(name,value));		
	}
	
}
