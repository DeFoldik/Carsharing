package com.carsharing.carsharing.service;

import com.carsharing.carsharing.cache.UserCache;
import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Renter;
import com.carsharing.carsharing.repository.BookingRepository;
import com.carsharing.carsharing.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RenterService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final UserCache userCache;

    public RenterService(UserRepository userRepository, BookingRepository bookingRepository,
                         UserCache userCache) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.userCache = userCache;
    }

    public List<Renter> getAllRenters() {
        return userRepository.findAllRenters();
    }

    public Renter getRenterById(Long id) {
        Renter renter = (Renter) userCache.get(id);
        if (renter == null) {
            renter = userRepository.findRenterById(id)
                    .orElseThrow(() -> new NotFound("Renter not found with ID: " + id));
            userCache.put(id, renter);
        }
        return renter;
    }

    public Renter createRenter(Renter renter) {
        Renter savedRenter = userRepository.save(renter);
        userCache.put(savedRenter.getId(), savedRenter);
        return savedRenter;
    }

    public void deleteRenter(Long renterId) {
        Renter renter = (Renter) userCache.get(renterId);

        if (renter == null) {
            renter = (Renter) userRepository.findById(renterId)
                    .orElseThrow(() -> new NotFound("Renter not found with ID: " + renterId));
        }

        // Удаляем все бронирования этого рентора
        List<Booking> bookings = bookingRepository.findByRenter(renter);
        for (Booking booking : bookings) {
            // Убираем связь с машинами
            for (Car car : booking.getCars()) {
                car.getBookings().remove(booking);
            }
            bookingRepository.delete(booking); // Удаляем саму бронь
        }

        userCache.remove(renterId);
        userRepository.delete(renter); // Удаляем рентора
    }

    public Renter updateRenter(Long id, @Valid Renter renterDetails) {
        Renter renter = (Renter) userCache.get(id);

        if (renter == null) {
            renter = (Renter) userRepository.findById(id)
                    .orElseThrow(() -> new NotFound("Renter not found with ID: " + id));
        }

        // Обновляем имя рентера
        renter.setName(renterDetails.getName());

        Renter updatedRenter = userRepository.save(renter);
        userCache.put(updatedRenter.getId(), updatedRenter);
        return updatedRenter;
    }
}

