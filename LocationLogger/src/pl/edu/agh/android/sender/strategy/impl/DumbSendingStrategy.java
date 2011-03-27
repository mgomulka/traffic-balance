package pl.edu.agh.android.sender.strategy.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import pl.edu.agh.android.components.LocationDataSource;
import pl.edu.agh.android.sender.DataSender;
import pl.edu.agh.android.sender.strategy.SendingStrategy;

public class DumbSendingStrategy implements SendingStrategy, Runnable {

	public static final long DEFAULT_SEDING_INTERVAL = 10000L;
	
	private DataSender sender;
	private LocationDataSource dataSource;
	private ScheduledExecutorService executor;
	
	private Future<?> taskFuture = null;
	
	private long interval;
	
	public DumbSendingStrategy(DataSender sender, LocationDataSource dataSource, long interval) {
		
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
		
		if(dataSource.getLocationData().size() > 0) {
			sender.sendAllData();
		}
	}
}
