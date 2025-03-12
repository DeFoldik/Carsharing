package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Renter;
import com.carsharing.carsharing.repository.UserRepository;
import com.carsharing.carsharing.repository.BookingRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RenterService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public RenterService(UserRepository userRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Renter> getAllRenters() {
        return userRepository.findAllRenters();
    }

    public Renter getRenterById(Long id) {
        return userRepository.findRenterById(id)
                .orElseThrow(() -> new NotFound("Renter not found with ID: " + id));
    }

    public Renter createRenter(Renter renter) {
        return userRepository.save(renter);
    }

    public void deleteRenter(Long renterId) {
        Renter renter = (Renter) userRepository.findById(renterId)
                .orElseThrow(() -> new NotFound("Renter not found with ID: " + renterId));

        // Удаляем все бронирования этого рентора
        List<Booking> bookings = bookingRepository.findByRenter(renter);
        for (Booking booking : bookings) {
            // Убираем связь с машинами
            for (Car car : booking.getCars()) {
                car.getBookings().remove(booking);
            }
            bookingRepository.delete(booking); // Удаляем саму бронь
        }

        userRepository.delete(renter); // Удаляем рентора
    }

    public Renter updateRenter(Long id, @Valid Renter renterDetails) {
        Renter renter = (Renter) userRepository.findById(id)
                .orElseThrow(() -> new NotFound("Renter not found with ID: " + id));

        // Обновляем имя рентера
        renter.setName(renterDetails.getName());

        // Сохраняем обновленного рентера
        return userRepository.save(renter);
    }
}

