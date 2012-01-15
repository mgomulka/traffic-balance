package pl.edu.agh.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

public class Simulator {

	private static FileParser parser;
	
	public static void main(String[] args) throws Exception {
		
		if(args.length < 2) {
			System.err.println("required args: <service url> <input file>");
			System.exit(-1);
		}
		
		String url = args[0];
		File inputFile = new File(args[1]);
		
		if(!inputFile.exists()) {
			throw new FileNotFoundException(args[1]);
		}
	
		LocationDataSender sender = new LocationDataSender(url, new Date());
		parser = new FileParser(inputFile, sender);
		parser.start();
		
		System.out.println("sending rate: " + sender.getSendingRate() + ", total vehicles: " + sender.getTotalCars());
		System.out.println("maximum single request time: " + sender.getMaxRequestTime() + " ms");
	}
	
}
