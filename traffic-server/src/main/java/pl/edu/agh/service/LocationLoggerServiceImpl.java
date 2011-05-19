package pl.edu.agh.service;

import org.springframework.stereotype.Component;

import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.LocationData;

@Component
public class LocationLoggerServiceImpl implements LocationLoggerService {

	@Override
	public void sendLocationData(LocationData arg0) throws JSONRPCException {
		// TODO Auto-generated method stub

	}

}
