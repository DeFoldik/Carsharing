package com.carsharing.carsharing.repository;

import com.carsharing.carsharing.model.Car;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByBrandIgnoreCase(String brand);  // Ищем машины по бренду (игнорируя регистр)
}