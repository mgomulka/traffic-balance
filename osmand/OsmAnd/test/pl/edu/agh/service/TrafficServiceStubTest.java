package pl.edu.agh.service;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pl.edu.agh.adhoc.PrintAdHocSocket;
import pl.edu.agh.jsonrpc.RawDataSocket;
import pl.edu.agh.logic.TrafficDataProvider;
import pl.edu.agh.logic.TrafficDataProvider.TrafficDataRequest;
import pl.edu.agh.logic.TrafficDataProvider.TrafficDataSet;
import pl.edu.agh.model.RectD;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.model.TrafficData;
import pl.edu.agh.model.TrafficInfo;

public class TrafficServiceStubTest {

	private RawDataSocket socket;
	private TrafficDataProvider provider;
	private List<SimpleLocationInfo> way = Arrays.asList(
			
			new SimpleLocationInfo(0.0, 0.0),
			new SimpleLocationInfo(1.0, 1.0),
			new SimpleLocationInfo(2.0, 2.0)
			);
	private List<TrafficInfo> infos = Arrays.asList(
			new TrafficInfo(way, 10.0, 10.0, true),
			new TrafficInfo(way, 20.0, 20.0, true),
			new TrafficInfo(way, 30.0, 30.0, true)
			); 
	
	private SimpleLocationInfo locInfo = new SimpleLocationInfo(20.0, 50.0);
	private int zoom = 10;
	
	
	@Before
	public void init() throws IOException {
	
		PipedInputStream pipeIn = new PipedInputStream();
		PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);
		
		socket = new PrintAdHocSocket(pipeIn, pipeOut);
		
		
		TrafficData data = new TrafficData(infos);
		TrafficDataSet dataSet = new TrafficDataSet(data, new RectD(10.0,10.0,10.0,10.0));
		TrafficDataRequest request = new TrafficDataRequest(locInfo, zoom, new Date());
		
		provider = new TrafficDataProvider(socket, request, dataSet);
		provider.startAdHocServer();
	
	}
	
	@Test
	public void testRequest() throws Exception {
		
		TrafficService service = provider.getTrafficService();
		TrafficData data = service.getTrafficDataViaAdHoc(locInfo, TrafficDataProvider.calculateRadiusForZoom(zoom));
		Assert.assertNotNull(data);
		System.out.println("SUCCESSFUL!");
	}
	
	@After
	public void finish() {
		provider.stopAdHocServer();
	}
	
}
