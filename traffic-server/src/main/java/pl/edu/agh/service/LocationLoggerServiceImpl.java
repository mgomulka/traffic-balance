package pl.edu.agh.service;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.springframework.stereotype.Component;

import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.LocationData;
import pl.edu.agh.model.LocationInfo;
import pl.edu.agh.model.RoutingResult;
import pl.edu.agh.model.SimpleLocationInfo;

@Component
public class LocationLoggerServiceImpl implements LocationLoggerService {

	@Override
	public RoutingResult sendLocationData(LocationData arg0) throws JSONRPCException {
		// tutaj trzeba bedzie podpiac wlasciwy algorytm map-matchingu
		// na potrzeby testowania bedzie zwracany i wyswietlany w gui
		// wlasciwy algorytm powinien znalezc sie w routingBO albo innym BO
		List<LocationInfo> locations = arg0.getLocationInfos();
		List<SimpleLocationInfo> infos = newArrayList();
		
		for (LocationInfo locInfo : locations) {
			infos.add(locInfo);
		}
		
		return new RoutingResult(infos);

	}

}
