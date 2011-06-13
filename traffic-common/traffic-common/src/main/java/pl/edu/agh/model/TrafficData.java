package pl.edu.agh.model;

import java.util.List;

public class TrafficData {

	private List<TrafficInfo> trafficInfos;

	public TrafficData(List<TrafficInfo> trafficInfos) {
		this.trafficInfos = trafficInfos;
	}

	public List<TrafficInfo> getTrafficInfos() {
		return trafficInfos;
	}

}
