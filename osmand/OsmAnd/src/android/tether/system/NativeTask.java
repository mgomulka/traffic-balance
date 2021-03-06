package android.tether.system;


import android.util.Log;

public class NativeTask {
    
	public static final String MSG_TAG = "AdHoc Wifi";

	static {
        try {
            Log.i(MSG_TAG, "Trying to load libnativetask.so");
            System.load("/data/data/net.osmand.plus/library/libnativetask.so");
        }
        catch (UnsatisfiedLinkError ule) {
            Log.e(MSG_TAG, "Could not load libnativetask.so");
        }
    }
    public static native String getProp(String name);
    public static native int runCommand(String command);
}
