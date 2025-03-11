package com.carsharing.carsharing.controller;

import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.service.CarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cars")
public class CarController {
    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    public List<Car> getCars() {
        return carService.getAllCars();
    }

    @GetMapping("/{id}")
    public Car getCarById(@PathVariable Long id) {
        return carService.getCarById(id);
    }

    // Теперь создаем машину с указанием ID владельца
    @PostMapping
    public List<Car> createCars(@RequestBody List<Car> cars, @RequestParam Long ownerId) {
        return carService.createCars(cars, ownerId);
    }

    @DeleteMapping("/{id}")
    public void deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
    }
}
