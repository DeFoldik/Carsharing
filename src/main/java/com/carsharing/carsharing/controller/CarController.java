package com.carsharing.carsharing.controller;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.service.CarService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    // Получение всех машин или фильтрация по бренду
    @GetMapping
    public List<Car> getCars(@RequestParam(required = false) String brand) {
        if (brand != null) {
            // Фильтрация по бренду
            List<Car> filteredCars = carService.getCarsByBrand(brand);
            if (filteredCars.isEmpty()) {
                // Если машины с указанным брендом нет
                throw new NotFound("No cars found for brand: " + brand);
            }
            return filteredCars;
        }
        // Если параметр brand не передан, вернуть все машины
        return carService.getAllCars();
    }

    // Получение машины по ID
    @GetMapping("/{id}")
    public Car getCarById(@PathVariable Long id) {
        return carService.getCarById(id);
    }

    // Добавление одной или нескольких машин
    @PostMapping
    public List<Car> createCars(@RequestBody @Valid List<Car> cars, @RequestParam Long ownerId) {
        return carService.createCars(cars, ownerId);
    }

    // Обновление машины по ID
    @PutMapping("/{id}")
    public Car updateCar(@PathVariable Long id, @RequestBody @Valid Car carDetails) {
        return carService.updateCar(id, carDetails);
    }

    // Удаление машины по ID
    @DeleteMapping("/{id}")
    public void deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
    }
}

