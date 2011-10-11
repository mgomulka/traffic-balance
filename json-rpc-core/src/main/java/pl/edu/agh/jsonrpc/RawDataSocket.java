package pl.edu.agh.jsonrpc;

import java.io.IOException;
import java.net.SocketException;

public interface RawDataSocket {

	void sendData(byte[] data) throws IOException;
	byte[] receiveData() throws IOException;
	boolean isOpened();
	void rebind() throws SocketException;
	void close();
}
