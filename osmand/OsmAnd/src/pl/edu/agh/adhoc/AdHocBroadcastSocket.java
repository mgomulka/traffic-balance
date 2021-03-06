package pl.edu.agh.adhoc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import pl.edu.agh.jsonrpc.RawDataSocket;
import android.util.Log;

public class AdHocBroadcastSocket implements RawDataSocket {

	public static final int RECEIVE_BUFFER_SIZE = 65535; 
	public static final String BROADCAST_ADDRESS = "255.255.255.255";	
	public DatagramSocket socket = null;
	public int port;
	
	
	public void open(InetAddress listenAddress, int port) throws SocketException {
		this.port = port;
		socket = new DatagramSocket(port);
		socket.setBroadcast(true);
	}
	
	public void rebind()  throws SocketException {
		socket = new DatagramSocket(this.port);
		socket.setBroadcast(true);
	}
	
	public boolean isOpened() {
		return socket != null && !socket.isClosed();
	}
	
	public synchronized void sendData(byte[] data) throws IOException {
		if(socket == null) {
			throw new IllegalStateException("socket closed");
		}
		if(socket.isClosed() || !socket.isBound()) {
			Log.i("Ad-Hoc Socket", "rebinding socket");
			rebind();
		}
		InetAddress address = InetAddress.getByName(BROADCAST_ADDRESS);
		DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
		socket.send(packet);
	}
	
	public synchronized byte[] receiveData() throws IOException {
		if(socket == null) {
			throw new IllegalStateException("socket closed");
		}
		byte[] buf = new byte[RECEIVE_BUFFER_SIZE];
		
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		return buf;
	}

	public void close() {
		if(socket == null) {
			return;
		}
		socket.close();
	}
}
