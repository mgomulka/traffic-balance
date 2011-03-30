package pl.edu.agh.servlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.agh.jsonrpc.JSONRPCHttpServlet;
import pl.edu.agh.skeleton.TrafficSkeleton;

@Component
public class TrafficServlet extends JSONRPCHttpServlet {

	@Autowired
	public TrafficServlet(TrafficSkeleton skeleton) {
		this.skeleton = skeleton;
	}

}
