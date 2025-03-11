package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Renter;
import com.carsharing.carsharing.model.User;
import com.carsharing.carsharing.repository.BookingRepository;
import com.carsharing.carsharing.repository.UserRepository;
import com.carsharing.carsharing.repository.CarRepository;

import java.util.List;
import org.springframework.stereotype.Service;


@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;

    public BookingService(BookingRepository bookingRepository, UserRepository userRepository,
                          CarRepository carRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
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
    public Booking createBooking(Long renterId, List<Long> carIds, Booking booking) {
        Renter renter = (Renter) userRepository.findById(renterId)
                .orElseThrow(() -> new NotFound("Renter not found with ID: " + renterId));

        // Получаем машины по их ID
        List<Car> cars = carRepository.findAllById(carIds);
        if (cars.isEmpty()) {
            throw new NotFound("Cars not found with provided IDs");
        }

        // Связываем арендатора и машины с бронированием
        booking.setRenter(renter);
        booking.setCars(cars); // Используем setCars, а не setCar

        return bookingRepository.save(booking);
    }

    // Обновление бронирования по ID
    public Booking updateBooking(Long id, Long renterId, List<Long> carIds,
                                 Booking bookingDetails) {
        // Находим бронирование по ID
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFound("Booking not found with ID: " + id));

        // Если передан новый арендатор, обновляем только его, если не null
        if (renterId != null) {
            // Находим пользователя по ID и проверяем, что это Renter
            User user = userRepository.findById(renterId)
                    .orElseThrow(() -> new NotFound("User not found with ID: " + renterId));

            if (!(user instanceof Renter)) {
                throw new NotFound("User with ID " + renterId + " is not a Renter");
            }

            // Приводим User к Renter
            Renter renter = (Renter) user;
            booking.setRenter(renter);
        }

        // Если переданы новые машины, обновляем их
        if (carIds != null && !carIds.isEmpty()) {
            List<Car> cars = carRepository.findAllById(carIds);
            if (cars.isEmpty()) {
                throw new NotFound("Cars not found with the provided IDs");
            }
            booking.setCars(cars);
        }

        // Обновляем только даты, если они переданы
        if (bookingDetails.getStartDate() != null) {
            booking.setStartDate(bookingDetails.getStartDate());
        }
        if (bookingDetails.getEndDate() != null) {
            booking.setEndDate(bookingDetails.getEndDate());
        }

        // Сохраняем обновленное бронирование
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
