package pl.edu.agh.adhoc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pl.edu.agh.jsonrpc.RawDataSocket;

public class PrintAdHocSocket implements RawDataSocket {

	
	private final InputStream in;
	private final OutputStream out;
	private boolean opened = true;
	private String name;
	
	public PrintAdHocSocket(String name, InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
		this.name = name;
	}
	
	@Override
	public void sendData(byte[] data) throws IOException {
		System.out.println(name + " sending:" + new String(data));
		out.write(data);
		out.flush();
	}

	@Override
	public byte[] receiveData() throws IOException {
		byte[] buffer = new byte[AdHocBroadcastSocket.RECEIVE_BUFFER_SIZE];
		in.read(buffer);
		System.out.println(name + " received:" + (buffer != null ? new String(buffer) : "null"));
		return buffer;
	}

	@Override
	public boolean isOpened() {
		return opened;
	}

	@Override
	public void close() {
		System.out.println(name + " closing socket");
		try {
		
			in.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		opened = false;
	}

}
