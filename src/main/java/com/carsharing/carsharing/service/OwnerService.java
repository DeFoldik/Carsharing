package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Owner;
import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.model.Renter;
import com.carsharing.carsharing.repository.UserRepository;
import com.carsharing.carsharing.repository.CarRepository;
import com.carsharing.carsharing.repository.BookingRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OwnerService {
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;

    public OwnerService(UserRepository userRepository, CarRepository carRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
    }


    public List<Owner> getAllOwners() {
        return userRepository.findAllOwners();
    }

    public Owner getOwnerById(Long id) {
        return userRepository.findOwnerById(id)
                .orElseThrow(() -> new NotFound("Owner not found with ID: " + id));
    }

    public Owner createOwner(Owner owner) {
        return userRepository.save(owner);
    }

    @Transactional
    public void deleteOwner(Long id) {
        Owner owner = (Owner) userRepository.findById(id)
                .orElseThrow(() -> new NotFound("Owner not found with ID: " + id));

        // Получаем все машины владельца
        List<Car> cars = carRepository.findByOwner(owner);
        if (cars.isEmpty()) {
            throw new NotFound("No cars found for the owner with ID: " + id);
        }

        // Удаляем все бронирования, связанные с машинами владельца
        for (Car car : cars) {
            List<Booking> bookings = bookingRepository.findByCars(car);
            for (Booking booking : bookings) {
                bookingRepository.delete(booking); // Удаляем бронирования
            }
        }

        // Удаляем все машины владельца
        carRepository.deleteAll(cars);

        // Удаляем владельца
        userRepository.delete(owner);
    }

    public Owner updateOwner(Long id, @Valid Owner ownerDetails) {
        Owner owner = (Owner) userRepository.findById(id)
                .orElseThrow(() -> new NotFound("Owner not found with ID: " + id));

        // Обновляем имя владельца
        owner.setName(ownerDetails.getName());

        // Если нужно обновить машины владельца, можно добавить логику здесь
        if (ownerDetails.getCars() != null) {
            owner.setCars(ownerDetails.getCars());
        }

        // Сохраняем обновленного владельца
        return userRepository.save(owner);
    }
}

