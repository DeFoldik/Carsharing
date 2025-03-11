package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Owner;
import com.carsharing.carsharing.repository.CarRepository;
import com.carsharing.carsharing.repository.OwnerRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final OwnerRepository ownerRepository;

    public CarService(CarRepository carRepository, OwnerRepository ownerRepository) {
        this.carRepository = carRepository;
        this.ownerRepository = ownerRepository;
    }

    // Получаем все машины
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    // Получаем машину по ID
    public Car getCarById(Long id) {
        return carRepository.findById(id).orElseThrow(() ->
                new NotFound("Car not found with ID: " + id));
    }

    // Получаем машины по бренду
    public List<Car> getCarsByBrand(String brand) {
        return carRepository.findByBrandIgnoreCase(brand);
    }

    // Добавление одной или нескольких машин
    public List<Car> createCars(List<Car> cars, Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new NotFound("Owner not found with ID: " + ownerId));

        // Привязываем владельца ко всем машинам
        for (Car car : cars) {
            car.setOwner(owner);
        }

        // Сохраняем все машины
        return carRepository.saveAll(cars);
    }

    // Обновление машины по ID
    public Car updateCar(Long id, Car carDetails) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new NotFound("Car not found with ID: " + id));

        // Обновляем поля машины
        car.setBrand(carDetails.getBrand());
        car.setModel(carDetails.getModel());

        // Сохраняем обновленную машину
        return carRepository.save(car);
    }

    // Удаление машины по ID
    public void deleteCar(Long id) {
        if (!carRepository.existsById(id)) {
            throw new NotFound("Car not found with ID: " + id);
        }
        carRepository.deleteById(id);
    }
}


