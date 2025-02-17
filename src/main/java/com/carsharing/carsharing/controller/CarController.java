package com.carsharing.carsharing.controller;

import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.service.CarService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    public List<Car> getCars(@RequestParam(required = false) String brand) {
        List<Car> allCars = carService.getAllCars();
        if (brand != null) {
            return allCars.stream()
                    .filter(car -> car.getBrand().equalsIgnoreCase(brand))
                    .toList();
        }
        return allCars;
    }

    @GetMapping("/{id}")
    public Car getCarById(@PathVariable String id) {
        return carService.getCarById(id);
    }
}