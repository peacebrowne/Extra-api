package com.example.extra.Services;

import com.example.extra.Entities.Location;

public interface LocationService {
    Location createLocation(String taskId, Location location) throws Exception;
    Location updateLocation(Location location);
    Location deleteLocation(String id);

    Location getLocation(String id);
}
