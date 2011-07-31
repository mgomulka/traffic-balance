package pl.edu.agh.service;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.agh.bo.RoutingBO;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.logic.Clock;
import pl.edu.agh.model.LocationData;
import pl.edu.agh.model.LocationInfo;
import pl.edu.agh.model.RoutingResult;
import pl.edu.agh.model.SimpleLocationInfo;

@Component
public class LocationLoggerServiceImpl implements LocationLoggerService {

	@Autowired
	private RoutingBO routingBO;
	
	private Clock clock = new Clock();
	
	@Override
	public List<RoutingResult> sendLocationData(LocationData arg0) throws JSONRPCException {
		// tutaj trzeba bedzie podpiac wlasciwy algorytm map-matchingu
		// na potrzeby testowania bedzie zwracany i wyswietlany w gui
		// wlasciwy algorytm powinien znalezc sie w routingBO albo innym BO
		List<LocationInfo> locations = arg0.getLocationInfos();
		List<SimpleLocationInfo> infos = newArrayList();
		
		for (LocationInfo locInfo : locations) {
			System.out.println(locInfo.getTime());
			infos.add(locInfo);
			locInfo.setTime(clock.nextSecond());
		}
		
		return routingBO.processLocationData(arg0);

	}

}
