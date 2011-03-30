package pl.edu.agh.jsonrpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class JSONRPCHttpServlet extends HttpServlet {
	
	protected JSONRPCSkeleton skeleton;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doPost(request, response);
		
		response.setContentType("application/json");
		
		BufferedReader in = request.getReader();
		PrintWriter out = response.getWriter();
		
		skeleton.processRequest(in, out);
	}

}
