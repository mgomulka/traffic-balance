package pl.edu.agh.android.sender.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import pl.edu.agh.android.components.LocationDataSource;
import pl.edu.agh.android.messageComposer.MessageComposer;
import pl.edu.agh.android.sender.DataSender;
import android.location.Location;
import android.util.Log;

public class RestfulDataSender implements DataSender {

	//set/get service URL from application configuration
	private static final String webServiceUrl = "http://192.168.192.50/";
	private static final String sendLocationDataMethod = "sendLocations";
	
	private LocationDataSource locationDataSource; 
	private MessageComposer composer;
	
	private DefaultHttpClient httpClient;
    private HttpContext localContext;
    
	
	public RestfulDataSender(LocationDataSource locationDataSource, MessageComposer messageComposer) {
		this.locationDataSource = locationDataSource;
		this.composer = messageComposer;
		
		HttpParams myParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(myParams, 10000);
        HttpConnectionParams.setSoTimeout(myParams, 10000);
        httpClient = new DefaultHttpClient(myParams);
        localContext = new BasicHttpContext();
	}
	
	public void sendAllData() {
		
		sendData(0, locationDataSource.getLocationData().size());		
	}
	
	public void sendData(int from, int to) {
		
		List<Location> data = locationDataSource.getLocationData();
		List<Location> toSend = new ArrayList<Location>();
		for(int i = from; i < to; i++) {
			
			Location loc = data.remove(i);
			toSend.add(loc);
		}
		String message = composer.composeLocationDataMessage(toSend);
		
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
 
        HttpPost httpPost = new HttpPost(webServiceUrl + sendLocationDataMethod);
        StringEntity tmp = null;        
 
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        
        try {
            tmp = new StringEntity(message,"UTF-8");
            
        } catch (UnsupportedEncodingException e) {
            Log.e("DataSender", "HttpUtils : UnsupportedEncodingException : "+e);
        }
 
        httpPost.setEntity(tmp);
        
        try {
            httpClient.execute(httpPost,localContext);
            
        } catch (Exception e) {
        	localContext = new BasicHttpContext();
            Log.e("DataSender", "HttpUtils: " + e);
        }
        
	}
}
