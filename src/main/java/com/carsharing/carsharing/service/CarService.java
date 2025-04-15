package com.carsharing.carsharing.service;

import com.carsharing.carsharing.cache.CarCache;
import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Owner;
import com.carsharing.carsharing.model.User;
import com.carsharing.carsharing.repository.BookingRepository;
import com.carsharing.carsharing.repository.CarRepository;
import com.carsharing.carsharing.repository.UserRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    private final CarCache carCache;

    public CarService(CarRepository carRepository, UserRepository userRepository,
                       BookingRepository bookingRepository, CarCache carCache) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.carCache = carCache;
    }

    // Получаем все машины
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    // Получаем машину по ID
    public Car getCarById(Long id) {
        //return carRepository.findById(id).orElseThrow(() ->
        //new NotFound("Car not found with ID: " + id));
        Car car = carCache.get(id);
        if (car == null) {
            log.info("Car with ID {} not found in cache, fetching from database", id);
            car = carRepository.findById(id)
                    .orElseThrow(() -> new NotFound("Car not found with ID: " + id));
            carCache.put(id, car);
        }
        return car;
    }

    // Получаем машины по бренду
    public List<Car> getCarsByBrand(String brand) {
        return carRepository.findByBrandIgnoreCase(brand);
    }

    /*@Transactional
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
        List<Car> savedCars = carRepository.saveAll(cars);

        // Добавляем сохраненные машины в кэш
        for (Car car : savedCars) {
            carCache.put(car.getId(), car);
        }

        return savedCars;
    }*/

    @Transactional
    public List<Car> createCars(List<Car> cars, Long ownerId) {

        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFound("Owner not found with ID: " + ownerId));

        if (!(user instanceof Owner owner)) {
            throw new NotFound("User with ID " + ownerId + " is not an Owner");
        }

        List<Car> updatedCars = cars.stream()
                .peek(car -> car.setOwner(owner))
                .toList();

        List<Car> savedCars = carRepository.saveAll(updatedCars);

        savedCars.forEach(car -> carCache.put(car.getId(), car));

        return savedCars;
    }

    @Transactional
    public Car updateCar(Long id, Car carDetails) {
        Car car = carCache.get(id);
        if (car == null) {
            car = carRepository.findById(id)
                    .orElseThrow(() -> new NotFound("Car not found with ID: " + id));
        }

        // Обновляем поля машины
        car.setBrand(carDetails.getBrand());
        car.setModel(carDetails.getModel());

        // Сохраняем обновленную машину
        Car updatedCar = carRepository.save(car);

        // Обновляем автомобиль в кэше
        carCache.put(id, updatedCar);

        return updatedCar;
    }

    @Transactional
    public void deleteCar(Long id) {
        Car car = carCache.get(id);
        if (car == null) {
            car = carRepository.findById(id)
                    .orElseThrow(() -> new NotFound("Car not found with ID: " + id));
        }

        // Удаляем все бронирования, связанные с машиной
        List<Booking> bookings = bookingRepository.findByCars(car);
        for (Booking booking : bookings) {
            bookingRepository.delete(booking); // Удаляем бронирование
        }

        // Удаляем машину из бд
        carRepository.deleteById(id);

        // Удаляем машину из кэша
        carCache.remove(id);
    }

    // Получение машин по модели (JPQL)
    public List<Car> getCarsByModel(String model) {
        return carRepository.findByModel(model);
    }

    public List<Car> getCarsByOwnerName(String ownerName) {
        return carRepository.findByOwnerName(ownerName);
    }

    // Поиск машин по имени владельца (Native Query)
    public List<Car> getCarsByOwnerNameNative(String ownerName) {
        return carRepository.findByOwnerNameNative(ownerName);
    }

}


