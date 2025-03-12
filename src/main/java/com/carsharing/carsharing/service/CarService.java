package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Owner;
import com.carsharing.carsharing.model.User;
import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.repository.CarRepository;
import com.carsharing.carsharing.repository.UserRepository;
import com.carsharing.carsharing.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public CarService(CarRepository carRepository, UserRepository userRepository, BookingRepository bookingRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
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
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFound("Owner not found with ID: " + ownerId));

        // Проверяем, что пользователь является владельцем
        if (!(user instanceof Owner)) {
            throw new NotFound("User with ID " + ownerId + " is not an Owner");
        }

        // Приводим User к Owner
        Owner owner = (Owner) user;

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

    @Transactional
    public void deleteCar(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new NotFound("Car not found with ID: " + id));

        // Удаляем все бронирования, связанные с машиной
        List<Booking> bookings = bookingRepository.findByCars(car);
        for (Booking booking : bookings) {
            bookingRepository.delete(booking); // Удаляем бронирование
        }

        // Удаляем машину
        carRepository.deleteById(id);
    }
}


