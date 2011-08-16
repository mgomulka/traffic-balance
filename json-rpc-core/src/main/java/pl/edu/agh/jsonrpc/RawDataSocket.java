package pl.edu.agh.jsonrpc;

import java.io.IOException;

public interface RawDataSocket {

	void sendData(byte[] data) throws IOException;
	byte[] receiveData() throws IOException;
	boolean isOpened();
	void close();
}
