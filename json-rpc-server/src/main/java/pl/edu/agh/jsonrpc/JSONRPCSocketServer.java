package pl.edu.agh.jsonrpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;

public class JSONRPCSocketServer {
	
	protected final JSONRPCSkeleton skeleton;
	private final RawDataSocket socket;
	
	public JSONRPCSocketServer(RawDataSocket socket, JSONRPCSkeleton skeleton) {
		this.socket = socket; 
		this.skeleton = skeleton;
	}

	public synchronized void start() throws JSONRPCException {
		if(!socket.isOpened()) {
			return;
		}
		try {
			byte[] buffer = socket.receiveData(); 
			
			while(buffer != null && buffer.length > 0) {
			
				BufferedReader reader = new BufferedReader(new StringReader(new String(buffer)));
				
				skeleton.processRequest(reader, new PrintWriter(new Writer() {
					@Override
					public void write(char[] cbuf, int off, int len) throws IOException {}

					@Override
					public void flush() throws IOException {}

					@Override
					public void close() throws IOException {}
				}));
				
				buffer = socket.receiveData();
			}
		} catch(IOException e) {
			throw new JSONRPCException("socket error", e);
		}
	}
	
	public synchronized void stop() {
		if(socket.isOpened()) {
			socket.close();
		}
	}
}
