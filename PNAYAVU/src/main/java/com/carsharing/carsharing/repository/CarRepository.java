package com.carsharing.carsharing.repository;

import com.carsharing.carsharing.model.Car;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CarRepository {
    private List<Car> cars = new ArrayList<>();

    public CarRepository() {
        cars.add(new Car("1", "Toyota", "Camry"));
        cars.add(new Car("2", "Honda", "Civic"));
        cars.add(new Car("3", "Ford", "Mustang"));
        cars.add(new Car("4", "Mercedes-Benz", "CLS"));
        cars.add(new Car("5", "Mercedes-Benz", "GLS"));
        cars.add(new Car("6", "Audi", "RS6"));
        cars.add(new Car("7", "BMW", "M5 F90"));
    }

    public List<Car> findAll() {
        return cars;
    }

    public Car findById(String id) {
        return cars.stream().filter(car -> car.getId().equals(id)).findFirst().orElse(null);
    }
}
