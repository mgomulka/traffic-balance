package pl.edu.agh.android.components;

import java.util.List;

import android.location.Location;

public interface LocationDataSource {

	List<Location> getLocationData();
}
