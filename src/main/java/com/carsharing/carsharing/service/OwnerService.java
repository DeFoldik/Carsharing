package com.carsharing.carsharing.service;

import com.carsharing.carsharing.cache.UserCache;
import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Owner;
import com.carsharing.carsharing.repository.BookingRepository;
import com.carsharing.carsharing.repository.CarRepository;
import com.carsharing.carsharing.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OwnerService {
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final UserCache userCache;

    public OwnerService(UserRepository userRepository, CarRepository carRepository,
                        BookingRepository bookingRepository, UserCache userCache) {
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
        this.userCache = userCache;
    }


    public List<Owner> getAllOwners() {
        return userRepository.findAllOwners();
    }

    public Owner getOwnerById(Long id) {
        Owner owner = (Owner) userCache.get(id);
        if (owner == null) {
            owner = userRepository.findOwnerById(id)
                    .orElseThrow(() -> new NotFound("Owner not found with ID: " + id));
            userCache.put(id, owner);
        }
        return owner;
    }

    public Owner createOwner(Owner owner) {
        Owner savedOwner = userRepository.save(owner);
        userCache.put(savedOwner.getId(), savedOwner);
        return savedOwner;
    }

    @Transactional
    public void deleteOwner(Long id) {
        Owner owner = (Owner) userCache.get(id);

        if (owner == null) {
            owner = (Owner) userRepository.findById(id)
                    .orElseThrow(() -> new NotFound("Owner not found with ID: " + id));
        }

        // Получаем машины владельца
        List<Car> cars = carRepository.findByOwner(owner);

        if (cars.isEmpty()) {
            System.out.println("Owner ID: " + id + " has no cars, deleting owner only.");
            userRepository.delete(owner); // Просто удаляем владельца
            return;
        }

        // Удаляем бронирования, связанные с машинами владельца
        for (Car car : cars) {
            List<Booking> bookings = bookingRepository.findByCars(car);
            bookingRepository.deleteAll(bookings);
        }

        // Удаляем машины владельца
        carRepository.deleteAll(cars);

        // Удаляем владельца
        userCache.remove(id);
        userRepository.delete(owner);
    }

    public Owner updateOwner(Long id, @Valid Owner ownerDetails) {
        Owner owner = (Owner) userCache.get(id);

        if (owner == null) {
            owner = (Owner) userRepository.findById(id)
                    .orElseThrow(() -> new NotFound("Owner not found with ID: " + id));
        }

        // Обновляем имя владельца
        owner.setName(ownerDetails.getName());

        // Если нужно обновить машины владельца, можно добавить логику здесь
        if (ownerDetails.getCars() != null) {
            owner.setCars(ownerDetails.getCars());
        }

        // Сохраняем обновленного владельца
        Owner updatedOwner = userRepository.save(owner);
        userCache.put(updatedOwner.getId(), updatedOwner);
        return updatedOwner;
    }
}

