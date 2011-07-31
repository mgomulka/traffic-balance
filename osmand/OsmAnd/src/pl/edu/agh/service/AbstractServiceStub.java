package pl.edu.agh.service;

import pl.edu.agh.jsonrpc.JSONRPCClient;
import pl.edu.agh.jsonrpc.JSONRPCHttpClient;

public abstract class AbstractServiceStub {
	
	private static final String SERVICE_URL_TEMPLATE = "http://%s/%s/%s";
	
	protected JSONRPCClient rpcClient;
	
	public AbstractServiceStub(String serviceName) {
		rpcClient = new JSONRPCHttpClient(String.format(SERVICE_URL_TEMPLATE, "192.168.1.45:8080", "traffic-server", serviceName));
	}

}
