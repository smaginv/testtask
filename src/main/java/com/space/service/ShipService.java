package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface ShipService {

    List<Ship> getAllShips(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                           Integer minCrewSize, Integer maxCrewSize, Double minRating, Double maxRating, ShipOrder order, Integer pageNumber, Integer pageSize);
    Long shipsCount(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                    Integer minCrewSize, Integer maxCrewSize, Double minRating, Double maxRating);
    Ship create (Ship ship);
    Boolean isValid (Ship ship);
    Double getRating (Ship ship);
    Ship getShipById (Long id);
    Boolean isValidId (Long id);
    Ship updateFields (Ship shipDB, Ship ship, HttpServletResponse response) throws IOException;
    Ship update (Ship ship);
    void delete (Long id);
}
