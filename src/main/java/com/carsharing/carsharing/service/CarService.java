package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.CarNotFound;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.repository.CarRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Car getCarById(String id) {

        Car car = carRepository.findById(id);
        if (car == null) {
            throw new CarNotFound("Car not found with ID: " + id);
        }
        return car;
    }
}
