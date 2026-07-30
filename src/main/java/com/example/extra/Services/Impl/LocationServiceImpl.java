package com.example.extra.Services.Impl;

import com.example.extra.Entities.Location;
import com.example.extra.Mappers.LocationMapper;
import com.example.extra.Services.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final LocationMapper locationMapper;

    @Override
    public Location createLocation(String taskId, Location location) throws Exception {
        return locationMapper.insertLocations(taskId, location);
    }

    @Override
    public Location updateLocation(Location location) {
        return null;
    }

    @Override
    public Location deleteLocation(String id) {
        return null;
    }

    @Override
    public Location getLocation(String id) {
        return locationMapper.getLocation(id);
    }
}
