package pl.edu.agh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.agh.bo.RoutingBO;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.LocationData;

@Component
public class LocationLoggerServiceImpl implements LocationLoggerService {

	@Autowired
	private RoutingBO routingBO;
	
	@Override
	public void sendLocationData(LocationData locationData) throws JSONRPCException {
		routingBO.processLocationData(locationData);
	}

}
