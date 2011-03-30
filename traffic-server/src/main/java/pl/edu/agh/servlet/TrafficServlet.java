package pl.edu.agh.servlet;

import javax.servlet.ServletException;

import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.edu.agh.jsonrpc.JSONRPCHttpServlet;
import pl.edu.agh.skeleton.TrafficSkeleton;

@Component
public class TrafficServlet extends JSONRPCHttpServlet {

	@Override
	public void init() throws ServletException {
		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		skeleton = (TrafficSkeleton) context.getBean(TrafficSkeleton.BEAN_NAME);
	}

}
