package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Owner;
import com.carsharing.carsharing.repository.CarRepository;
import com.carsharing.carsharing.repository.OwnerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarService {
    private final CarRepository carRepository;
    private final OwnerRepository ownerRepository;

    public CarService(CarRepository carRepository, OwnerRepository ownerRepository) {
        this.carRepository = carRepository;
        this.ownerRepository = ownerRepository;
    }

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Car getCarById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new NotFound("Car not found with ID: " + id));
    }

    public List<Car> createCars(List<Car> cars, Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new NotFound("Owner not found with ID: " + ownerId));

        for (Car car : cars) {
            car.setOwner(owner);
        }

        return carRepository.saveAll(cars);  // Сохраняем все машины
    }


    public void deleteCar(Long id) {
        carRepository.deleteById(id);
    }
}

