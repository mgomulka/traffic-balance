package pl.edu.agh.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pl.edu.agh.model.TrafficData;
import pl.edu.agh.model.TrafficInfo;

public class TrafficDataBuffer {

	
	private Map<Long, TrafficDataCollector> collectedData = new HashMap<Long, TrafficDataCollector>();
	private List<TrafficData> fullData = new ArrayList<TrafficData>();
	
	public boolean appendChunk(TrafficData data, long sender, int seq, boolean lastChunk) {
		
		TrafficDataCollector collector = collectedData.get(sender);
		if(collector == null) {
			collector = new TrafficDataCollector();
			collectedData.put(sender, collector);
		}

		collector.appendChunk(data, seq, lastChunk);
		if(collector.collectedAll()) {
			fullData.add(collector.getData());
			return true;
		}
		return false;
	}
	
	public List<TrafficData> getCollectedData() {
		return fullData;
	}
	
	public void discard() {
		collectedData.clear();
		fullData.clear();
	}
	
	private static class TrafficDataCollector {
		
		private int chunksTotal = 0;
		private Map<Integer, TrafficData> chunks = new TreeMap<Integer, TrafficData>(); 
		
		public void appendChunk(TrafficData data, int seq, boolean last) {
			chunks.put(seq, data);
			
			if(last) {
				chunksTotal = seq+1;
			}
		}
		
		public boolean collectedAll() {
			
			return chunksTotal > 0 && chunks.size() == chunksTotal;
		}
				
		public TrafficData getData() {
			if(!collectedAll()) {
				return null;
			}
			List<TrafficInfo> infos = new ArrayList<TrafficInfo>();
			for(TrafficData data : chunks.values()) {
				infos.addAll(data.getTrafficInfos());
			}
			
			return new TrafficData(infos);
		}
		
	}
}
