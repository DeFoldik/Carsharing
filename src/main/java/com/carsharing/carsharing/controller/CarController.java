package com.carsharing.carsharing.controller;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/cars")
@Tag(name = "Автомобили", description = "Управление автомобилями")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    // Получение всех машин или фильтрация по бренду
    /*@GetMapping
    @Operation(summary = "Получить автомобили", description = "Возвращает автомобили")
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
    }*/

    // Получение машины по ID
    @GetMapping("/{id}")
    @Operation(summary = "Получить автомобиль по ID",
            description = "Возвращает автомобиль по идентификатору")
    public Car getCarById(@PathVariable Long id) {
        return carService.getCarById(id);
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    @Operation(summary = "Выложить автомобиль", description = "Добавляет автомобиль")
    public List<Car> createCars(@RequestBody @Valid List<Car> cars, @RequestParam Long ownerId) {
        return carService.createCars(cars, ownerId);
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/my")
    public ResponseEntity<List<Car>> getMyCars(Authentication authentication) {
        String username = authentication.getName();
        List<Car> cars = carService.getCarsByUsername(username);
        return ResponseEntity.ok(cars);

    }

    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{id}")
    @Operation(summary = "Обновить автомобиль по ID",
            description = "Обновляет автомобиль по идентификатору")
    public Car updateCar(@PathVariable Long id, @RequestBody @Valid Car carDetails) {
        return carService.updateCar(id, carDetails);
    }

    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить автомобиль по ID",
            description = "Удаляет автомобиль по идентификатору")
    public void deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
    }

    // Получение машин по модели (JPQL)
    @GetMapping("/by-model")
    @Operation(summary = "Получить автомобили по модели",
            description = "Возвращает автомобили по идентификатору")
    public List<Car> getCarsByModel(@RequestParam String model) {
        return carService.getCarsByModel(model);
    }

    @GetMapping("/by-owner")
    @Operation(summary = "Получить автомобили по владельцу",
            description = "Возвращает автомобили по владелицу")
    public List<Car> getCarsByOwnerName(@RequestParam String ownerName) {
        return carService.getCarsByOwnerName(ownerName);
    }

    @GetMapping
    public List<Car> getCars(@RequestParam(required = false) String brand,
                             @RequestParam(required = false) String model) {

        if (brand != null && model != null) {
            List<Car> cars = carService.getCarsByBrand(brand)
                    .stream()
                    .filter(car -> car.getModel().equalsIgnoreCase(model))
                    .toList();

            if (cars.isEmpty()) {
                throw new NotFound("Нет машин бренда '" + brand + "' с моделью '" + model + "'");
            }

            return cars;
        }

        if (brand != null) {
            List<Car> cars = carService.getCarsByBrand(brand);
            if (cars.isEmpty()) throw new NotFound("Нет машин с брендом: " + brand);
            return cars;
        }

        return carService.getAllCars();
    }

    @GetMapping("/by-owner-native")
    @Operation(summary = "Получить автомобили по владельцу",
            description = "Возвращает автомобили по владелицу")
    public List<Car> getCarsByOwnerNameNative(@RequestParam String ownerName) {
        return carService.getCarsByOwnerNameNative(ownerName);
    }
}

