package pl.edu.agh.logic;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import pl.edu.agh.service.LocationDataSource;
import android.location.Location;

public class PeriodicSendingStrategy implements SendingStrategy, Runnable {
	
	private LocationDataSender sender;
	private LocationDataSource dataSource;
	private ScheduledExecutorService executor;
	
	private ScheduledFuture<?> taskFuture = null;
	
	private long interval;
	
	public PeriodicSendingStrategy(LocationDataSender sender, LocationDataSource dataSource, long interval) {
		
		this.sender = sender;
		this.dataSource = dataSource;
		this.interval = interval;
		executor = Executors.newSingleThreadScheduledExecutor();
	}
	
	
	public void activate() {
		if(taskFuture == null) {
			taskFuture = executor.scheduleAtFixedRate(this, 0, interval, TimeUnit.MILLISECONDS);
		}
	}
	
	public void deactivate() {
		if(taskFuture != null) {
			taskFuture.cancel(false);
			taskFuture = null;
		}
	}
	
	public void run() {
		List<Location> locations = dataSource.getAndRemoveAllData();
		if (!locations.isEmpty()) {
			sender.sendAllData(locations);
		}
	}
}
