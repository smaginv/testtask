package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShipServiceImpl implements ShipService {

    private ShipRepository shipRepository;
    private static final Double MIN_SPEED = 0.01;
    private static final Double MAX_SPEED = 0.99;
    private static final Integer MIN_CREW_SIZE = 1;
    private static final Integer MAX_CREW_SIZE = 9999;
    private static final Integer MIN_PROD_YEAR = 2800;
    private static final Integer CURR_YEAR = 3019;
    private static final Integer STRING_LENGTH = 50;

    @Autowired
    public ShipServiceImpl (ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    @Override
    public List<Ship> getAllShips(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                                  Integer minCrewSize, Integer maxCrewSize, Double minRating, Double maxRating, ShipOrder order, Integer pageNumber, Integer pageSize) {

        if (pageNumber == null) {
            pageNumber = 0;
        }
        if (pageSize == null) {
            pageSize = 3;
        }
        if (order == null) {
            order = ShipOrder.ID;
        }

        return filterShipList(shipRepository.findAll(), name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating)
                .stream().sorted(getComparator(order)).skip(pageNumber * pageSize).limit(pageSize).collect(Collectors.toList());
    }

    @Override
    public Long shipsCount(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                           Integer minCrewSize, Integer maxCrewSize, Double minRating, Double maxRating) {
        return (long) filterShipList(shipRepository.findAll(), name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating).size();

    }

    @Override
    public Ship create (Ship ship) {
        return shipRepository.saveAndFlush(ship);
    }

    @Override
    public Ship getShipById(Long id) {
        return shipRepository.findById(id).orElse(null);
    }

    @Override
    public Ship update(Ship ship) {
        return shipRepository.saveAndFlush(ship);
    }

    @Override
    public void delete(Long id) {
        shipRepository.deleteById(id);
    }

    @Override
    public Boolean isValid (Ship ship) {
        if (ship.getName() == null || ship.getPlanet() == null || ship.getShipType() == null ||
                ship.getProdDate() == null || ship.getSpeed() == null || ship.getCrewSize() == null) {
            return false;
        }
        if (!validStringField(ship.getName())) {
            return false;
        }
        if (!validStringField(ship.getPlanet())) {
            return false;
        }
        if (!validSpeed(ship.getSpeed())) {
            return false;
        }
        if (!validCrewSize(ship.getCrewSize())) {
            return false;
        }

        return validProdDate(ship.getProdDate());
    }

    @Override
    public Ship updateFields (Ship shipDB, Ship ship, HttpServletResponse response) throws IOException {
        if (ship.getName() != null) {
            if (!validStringField(ship.getName())) {
                response.sendError(400);
                return null;
            }
            shipDB.setName(ship.getName());
        }
        if (ship.getPlanet() != null) {
            if (!validStringField(ship.getPlanet())) {
                response.sendError(400);
                return null;
            }
            shipDB.setPlanet(ship.getPlanet());
        }
        if (ship.getShipType() != null) {
            shipDB.setShipType(ship.getShipType());
        }
        if (ship.getProdDate() != null) {
            if (!validProdDate(ship.getProdDate())) {
                response.sendError(400);
                return null;
            }
            shipDB.setProdDate(ship.getProdDate());
        }
        if (ship.isUsed() != null) {
            shipDB.setUsed(ship.isUsed());
        }
        if (ship.getSpeed() != null) {
            if (!validSpeed(ship.getSpeed())) {
                response.sendError(400);
                return null;
            }
            shipDB.setSpeed(ship.getSpeed());
        }
        if (ship.getCrewSize() != null) {
            if (!validCrewSize(ship.getCrewSize())) {
                response.sendError(400);
                return null;
            }
            shipDB.setCrewSize(ship.getCrewSize());
        }

        return shipDB;
    }

    @Override
    public Double getRating (Ship ship) {
        Double v = ship.getSpeed();
        Double k = ship.isUsed() ? 0.5 : 1;
        Integer prodYear = getYear(ship.getProdDate());

        return Math.round((80 * v * k) / (CURR_YEAR - prodYear + 1) * 100) / 100D;
    }

    @Override
    public Boolean isValidId (Long id) {
        return id > 0;
    }

    private Comparator<Ship> getComparator (ShipOrder order) {
        switch (order.getFieldName()) {
            case "speed":
                return Comparator.comparing(Ship::getSpeed);
            case "prodDate":
                return Comparator.comparing(Ship::getProdDate);
            case "rating":
                return Comparator.comparing(Ship::getRating);
            default:
                return Comparator.comparing(Ship::getId);
        }
    }

    private Boolean validStringField (String string) {
        return string != null && string.length() <= STRING_LENGTH && !string.isEmpty();
    }

    private Boolean validSpeed (Double speed) {
        return speed >= MIN_SPEED && speed <= MAX_SPEED;
    }

    private Boolean validCrewSize (Integer crewSize) {
        return crewSize >= MIN_CREW_SIZE && crewSize <= MAX_CREW_SIZE;
    }

    private Boolean validProdDate (Date prodDate) {
        if (prodDate.getTime() < 0) {
            return false;
        }
        return getYear(prodDate) >= MIN_PROD_YEAR && getYear(prodDate) <= CURR_YEAR;
    }

    private Integer getYear (Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    private List<Ship> filterShipList (List<Ship> ships, String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed, Double minSpeed,
                                       Double maxSpeed, Integer minCrewSize, Integer maxCrewSize, Double minRating, Double maxRating) {

        if (name != null) {
            ships = ships.stream().filter(ship -> ship.getName().contains(name)).collect(Collectors.toList());
        }
        if (planet != null) {
            ships = ships.stream().filter(ship -> ship.getPlanet().contains(planet)).collect(Collectors.toList());
        }
        if (shipType != null) {
            ships = ships.stream().filter(ship -> ship.getShipType() == shipType).collect(Collectors.toList());
        }
        if (after != null) {
            ships = ships.stream().filter(ship -> ship.getProdDate().getTime() >= after).collect(Collectors.toList());
        }
        if (before != null) {
            ships = ships.stream().filter(ship -> ship.getProdDate().getTime() <= before).collect(Collectors.toList());
        }
        if (isUsed != null) {
            ships = ships.stream().filter(ship -> ship.isUsed() == isUsed).collect(Collectors.toList());
        }
        if (minSpeed != null) {
            ships = ships.stream().filter(ship -> ship.getSpeed() >= minSpeed).collect(Collectors.toList());
        }
        if (maxSpeed != null) {
            ships = ships.stream().filter(ship -> ship.getSpeed() <= maxSpeed).collect(Collectors.toList());
        }
        if (minCrewSize != null) {
            ships = ships.stream().filter(ship -> ship.getCrewSize() >= minCrewSize).collect(Collectors.toList());
        }
        if (maxCrewSize != null) {
            ships = ships.stream().filter(ship -> ship.getCrewSize() <= maxCrewSize).collect(Collectors.toList());
        }
        if (minRating != null) {
            ships = ships.stream().filter(ship -> ship.getRating() >= minRating).collect(Collectors.toList());
        }
        if (maxRating != null) {
            ships = ships.stream().filter(ship -> ship.getRating() <= maxRating).collect(Collectors.toList());
        }
        return ships;
    }

}
