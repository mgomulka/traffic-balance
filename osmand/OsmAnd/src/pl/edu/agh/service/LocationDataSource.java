package pl.edu.agh.service;

import java.util.List;

import android.location.Location;

public interface LocationDataSource {

	List<Location> getAndRemoveAllData();
}
