package pl.edu.agh.android.messageComposer;

import java.util.List;

import android.location.Location;

public interface MessageComposer {

	String composeLocationDataMessage(List<Location> locations);
	
}
