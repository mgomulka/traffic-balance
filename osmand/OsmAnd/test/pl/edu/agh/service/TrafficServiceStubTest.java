package pl.edu.agh.service;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
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

	private RawDataSocket clientSocket;
	private RawDataSocket serverSocket;
	
	private TrafficDataProvider clientProvider;
	private TrafficDataProvider serverProvider;
		
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
	private int pipeBufferSize = 900;
	
	@Before
	public void init() throws IOException {
	
		PipedInputStream clientIn = new PipedInputStream();
		PipedOutputStream serverOut = new PipedOutputStream(clientIn);
		
		PipedInputStream serverIn = new PipedInputStream();
		PipedOutputStream clientOut = new PipedOutputStream(serverIn);
		
		clientSocket = new PrintAdHocSocket("Client", clientIn, clientOut);
		serverSocket = new PrintAdHocSocket("Server", serverIn, serverOut);
		
		clientProvider = new TrafficDataProvider(clientSocket);
		serverProvider = new TrafficDataProvider(serverSocket);
		
		clientProvider.startAdHocServer();
		serverProvider.startAdHocServer();
		
	
	}
	
	
	@Test
	public void testRequest() throws Exception {
		
		TrafficData data = new TrafficData(infos);
		TrafficDataSet dataSet = new TrafficDataSet(data, new RectD(10.0,10.0,10.0,10.0));
		TrafficDataRequest request = new TrafficDataRequest(locInfo, zoom, new Date());

		int sentSize = infos.size();
		
		clientProvider.injectLastTrafficOperations(request, dataSet, pipeBufferSize);
		serverProvider.injectLastTrafficOperations(request, dataSet, pipeBufferSize);
		
		TrafficService service = clientProvider.getTrafficService();
		TrafficData response = service.getTrafficDataViaAdHoc(locInfo, TrafficDataProvider.calculateRadiusForZoom(zoom));
		
		Assert.assertNotNull(response);
		Assert.assertEquals(sentSize, response.getTrafficInfos().size());
		
		System.out.println("SUCCESSFUL!");
	}
	
	@Test
	public void testBulkRequest() throws Exception {
		
		List<TrafficInfo> bulkInfos = new ArrayList<TrafficInfo>();
		for(int i=0; i < 300; i++) {
			bulkInfos.addAll(infos);
		}
		TrafficData data = new TrafficData(bulkInfos);
		TrafficDataSet dataSet = new TrafficDataSet(data, new RectD(10.0,10.0,10.0,10.0));
		TrafficDataRequest request = new TrafficDataRequest(locInfo, zoom, new Date());

		int sentSize = bulkInfos.size();
		
		clientProvider.injectLastTrafficOperations(request, dataSet, pipeBufferSize);
		serverProvider.injectLastTrafficOperations(request, dataSet, pipeBufferSize);
		
		TrafficService service = clientProvider.getTrafficService();
		TrafficData response = service.getTrafficDataViaAdHoc(locInfo, TrafficDataProvider.calculateRadiusForZoom(zoom));
		
		Assert.assertNotNull(response);
		Assert.assertEquals(sentSize, response.getTrafficInfos().size());
		
		System.out.println("SUCCESSFUL!");
	}
	
	@After
	public void finish() {
		serverProvider.stopAdHocServer();
		clientProvider.stopAdHocServer();
	}
	
}
