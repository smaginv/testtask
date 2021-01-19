package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/rest/ships")
public class ShipController {

    private ShipService shipService;

    @Autowired
    public ShipController (ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping()
    public List<Ship> getShipsList (@RequestParam(value = "name", required = false) String name,
                                    @RequestParam(value = "planet", required = false) String planet,
                                    @RequestParam(value = "shipType", required = false) ShipType shipType,
                                    @RequestParam(value = "after", required = false) Long after,
                                    @RequestParam(value = "before", required = false) Long before,
                                    @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                    @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                    @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                    @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                    @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                    @RequestParam(value = "minRating", required = false) Double minRating,
                                    @RequestParam(value = "maxRating", required = false) Double maxRating,
                                    @RequestParam(value = "order", required = false) ShipOrder order,
                                    @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                    @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return shipService.getAllShips(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating, order, pageNumber, pageSize);
    }

    @GetMapping("/count")
    public Long getShipsCount (@RequestParam(value = "name", required = false) String name,
                               @RequestParam(value = "planet", required = false) String planet,
                               @RequestParam(value = "shipType", required = false) ShipType shipType,
                               @RequestParam(value = "after", required = false) Long after,
                               @RequestParam(value = "before", required = false) Long before,
                               @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                               @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                               @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                               @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                               @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                               @RequestParam(value = "minRating", required = false) Double minRating,
                               @RequestParam(value = "maxRating", required = false) Double maxRating) {
        return shipService.shipsCount(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating);
    }

    @PostMapping()
    @ResponseBody
    public Ship createShip (@RequestBody Ship ship, HttpServletResponse response) throws IOException {
        if (!shipService.isValid(ship)) {
            response.sendError(400);
            return null;
        }
        if (ship.isUsed() == null) {
            ship.setUsed(false);
        }
        ship.setRating(shipService.getRating(ship));
        return shipService.create(ship);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Ship getShip (@PathVariable("id") Long id, HttpServletResponse response) throws IOException {
        if (!shipService.isValidId(id)) {
            response.sendError(400);
            return null;
        }
        Ship ship = shipService.getShipById(id);
        if (ship == null) {
            response.sendError(404);
        }
        return ship;
    }

    @PostMapping("/{id}")
    @ResponseBody
    public Ship updateShip (@PathVariable("id") Long id, @RequestBody Ship ship, HttpServletResponse response) throws IOException {

        if (!shipService.isValidId(id)) {
            response.sendError(400);
            return null;
        }
        Ship shipDB = shipService.getShipById(id);
        if (shipDB == null) {
            response.sendError(404);
            return null;
        }

        shipService.updateFields(shipDB, ship, response);
        shipDB.setRating(shipService.getRating(shipDB));

        return shipService.update(shipDB);
    }

    @DeleteMapping("/{id}")
    public void deleteShip (@PathVariable("id") Long id, HttpServletResponse response) throws IOException {
        if (!shipService.isValidId(id)) {
            response.sendError(400);
            return;
        }
        Ship ship = shipService.getShipById(id);
        if (ship == null) {
            response.sendError(404);
            return;
        }
        shipService.delete(id);
    }
}
