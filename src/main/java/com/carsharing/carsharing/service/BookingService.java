package com.carsharing.carsharing.service;

import com.carsharing.carsharing.cache.BookingCache;
import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Renter;
import com.carsharing.carsharing.model.User;
import com.carsharing.carsharing.repository.BookingRepository;
import com.carsharing.carsharing.repository.CarRepository;
import com.carsharing.carsharing.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final BookingCache bookingCache;

    public BookingService(BookingRepository bookingRepository, UserRepository userRepository,
                          CarRepository carRepository, BookingCache bookingCache) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.bookingCache = bookingCache;
    }

    @Transactional
    // Получение всех бронирований
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Получение бронирования по ID
    public Booking getBookingById(Long id) {
        //return bookingRepository.findById(id)
        //.orElseThrow(() -> new NotFound("Booking not found with ID: " + id));

        return bookingCache.getOrFetch(id, key -> bookingRepository.findById(key)
                .orElseThrow(() -> new NotFound("Booking not found with ID: " + key)));
    }

    @Transactional
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

        Booking savedBooking = bookingRepository.save(booking);
        bookingCache.put(savedBooking.getId(), savedBooking); // Добавляем в кэш
        return savedBooking;
    }

    public Booking updateBooking(Long id, Long renterId, List<Long> carIds,
                                 Booking bookingDetails) {
        Booking booking = bookingCache.get(id);
        if (booking == null) {
            booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new NotFound("Booking not found with ID: " + id));
        }

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
        Booking updatedBooking = bookingRepository.save(booking);
        bookingCache.put(updatedBooking.getId(), updatedBooking); // Обновляем в кэше
        return updatedBooking;

    }

    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFound("Booking not found with ID: " + id));



        // Удаляем связи с машинами
        for (Car car : booking.getCars()) {
            car.getBookings().remove(booking); // Удаляем бронь из списка бронирований машины
        }

        // Очищаем список машин в брони
        booking.getCars().clear();

        // Сохраняем изменения (обновляем машины)
        carRepository.saveAll(booking.getCars());

        // Удаляем бронь
        bookingRepository.delete(booking);
        bookingCache.remove(id);
    }
}
