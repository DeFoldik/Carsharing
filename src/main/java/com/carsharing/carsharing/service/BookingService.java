package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Renter;
import com.carsharing.carsharing.repository.BookingRepository;
import com.carsharing.carsharing.repository.CarRepository;
import com.carsharing.carsharing.repository.RenterRepository;
import java.util.List;
import org.springframework.stereotype.Service;


@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RenterRepository renterRepository;
    private final CarRepository carRepository;

    public BookingService(BookingRepository bookingRepository, RenterRepository renterRepository,
                          CarRepository carRepository) {
        this.bookingRepository = bookingRepository;
        this.renterRepository = renterRepository;
        this.carRepository = carRepository;
    }

    // Получение всех бронирований
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Получение бронирования по ID
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFound("Booking not found with ID: " + id));
    }

    // Создание нового бронирования
    public Booking createBooking(Long renterId, Long carId, Booking booking) {
        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new NotFound("Renter not found with ID: " + renterId));

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new NotFound("Car not found with ID: " + carId));

        // Связываем арендатора и машину с бронированием
        booking.setRenter(renter);
        booking.setCar(car);

        return bookingRepository.save(booking);
    }

    // Обновление бронирования по ID
    public Booking updateBooking(Long id, Long renterId, Long carId, Booking bookingDetails) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFound("Booking not found with ID: " + id));

        // Если передан новый арендатор, обновляем только его, если не null
        if (renterId != null) {
            Renter renter = renterRepository.findById(renterId)
                    .orElseThrow(() -> new NotFound("Renter not found with ID: " + renterId));
            booking.setRenter(renter);
        }

        // Если передана новая машина, обновляем только её, если не null
        if (carId != null) {
            Car car = carRepository.findById(carId)
                    .orElseThrow(() -> new NotFound("Car not found with ID: " + carId));
            booking.setCar(car);
        }

        // Обновляем только даты, если они переданы
        if (bookingDetails.getStartDate() != null) {
            booking.setStartDate(bookingDetails.getStartDate());
        }
        if (bookingDetails.getEndDate() != null) {
            booking.setEndDate(bookingDetails.getEndDate());
        }

        return bookingRepository.save(booking);
    }

    // Удаление бронирования
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new NotFound("Booking not found with ID: " + id);
        }
        bookingRepository.deleteById(id);
    }
}
