package com.carsharing.carsharing.controller;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.service.CarService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    /*@GetMapping
    public List<Car> getCars(@RequestParam(required = false) String brand) {
        List<Car> allCars = carService.getAllCars();
        if (brand != null) {
            List<Car> filteredCars = allCars.stream()
                    .filter(car -> car.getBrand().equalsIgnoreCase(brand))
                    .toList();
            if (filteredCars.isEmpty()) {
                throw new NotFound("No cars found for brand: " + brand);
            }
            return filteredCars;
        }
        return allCars;
    }

    @GetMapping("/{id}")
    public Car getCarById(@PathVariable String id) {
        return carService.getCarById(id);
    }*/

    @GetMapping
    public List<Car> getCars(@RequestParam(required = false) String brand) {
        return carService.getAllCars();
    }

    @GetMapping("/{id}")
    public Car getCarById(@PathVariable String id) {
        return carService.getCarById(id);
    }

    @PostMapping
    public Car createCar(@RequestBody Car car) {
        return carService.createCar(car);
    }

    @PutMapping("/{id}")
    public Car updateCar(@PathVariable String id, @RequestBody Car carDetails) {
        return carService.updateCar(id, carDetails);
    }

    @DeleteMapping("/{id}")
    public void deleteCar(@PathVariable String id) {
        carService.deleteCar(id);
    }
}
