package pl.edu.agh.android.sender.impl;

import java.util.ArrayList;
import java.util.List;

import pl.edu.agh.android.components.LocationDataSource;
import pl.edu.agh.android.sender.DataSender;
import pl.edu.agh.assembler.LocationDataBatchAssembler;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.LocationDataBatch;
import pl.edu.agh.service.TrafficService;
import pl.edu.agh.service.TrafficServiceStub;
import android.location.Location;
import android.util.Log;

public class DataSenderImpl implements DataSender {

	private static final String SERVER_URL = "192.168.1.105:8080";
	
	private LocationDataSource locationDataSource;
	private TrafficService trafficService;

	public DataSenderImpl(LocationDataSource locationDataSource) {
		this.locationDataSource = locationDataSource;
		this.trafficService = new TrafficServiceStub(SERVER_URL, "traffic-server");
	}

	public void sendAllData() {

		sendData(0, locationDataSource.getLocationData().size());
	}

	public void sendData(int from, int to) {

		List<Location> data = locationDataSource.getLocationData();
		List<Location> toSend = new ArrayList<Location>();
		for (int i = from; i < to; i++) {
			Location loc = data.remove(i);
			toSend.add(loc);
		}

		try {
			LocationDataBatch batchToSend = LocationDataBatchAssembler.convert(toSend);
			trafficService.sendTrafficData(batchToSend);
		} catch (JSONRPCException ex) {
			Log.e("DataSender", "Error while sending data to server", ex);
		}

	}
}
