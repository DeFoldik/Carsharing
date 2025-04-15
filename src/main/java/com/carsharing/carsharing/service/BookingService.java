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
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
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
        // 1. Проверка арендатора
        Renter renter = (Renter) userRepository.findById(renterId)
                .orElseThrow(() -> new NotFound("Renter not found with ID: " + renterId));

        // 2. Проверка автомобилей
        List<Car> cars = carRepository.findAllById(carIds);
        if (cars.isEmpty() || cars.size() != carIds.size()) {
            throw new NotFound("One or more cars not found with provided IDs");
        }

        // 3. Сохраняем для активации @PrePersist
        Booking savedBooking = bookingRepository.save(booking);

        // 4. Проверка доступности машин (после парсинга дат)
        checkCarAvailability(carIds, savedBooking.getStartDate(), savedBooking.getEndDate());

        // 5. Связываем сущности
        savedBooking.setRenter(renter);
        savedBooking.setCars(cars);

        // 6. Сохраняем окончательную версию
        Booking finalBooking = bookingRepository.save(savedBooking);
        bookingCache.put(finalBooking.getId(), finalBooking);
        return finalBooking;
    }

    @Transactional
    public Booking updateBooking(Long id, Long renterId, List<Long> carIds,
                                 Booking bookingDetails) {
        // Получаем существующее бронирование
        Booking booking = bookingCache.get(id);
        if (booking == null) {
            booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new NotFound("Booking not found with ID: " + id));
        }

        // Обновление арендатора (если передан)
        if (renterId != null) {
            User user = userRepository.findById(renterId)
                    .orElseThrow(() -> new NotFound("User not found with ID: " + renterId));

            if (!(user instanceof Renter)) {
                throw new NotFound("User with ID " + renterId + " is not a Renter");
            }
            booking.setRenter((Renter) user);
        }

        // Подготовка списка машин для проверки доступности
        List<Long> carsToCheck = carIds != null ? carIds :
                booking.getCars().stream()
                        .map(Car::getId)
                        .collect(Collectors.toList());

        // Обновление автомобилей (если переданы)
        if (carIds != null && !carIds.isEmpty()) {
            List<Car> cars = carRepository.findAllById(carIds);
            if (cars.isEmpty()) {
                throw new NotFound("Cars not found with the provided IDs");
            }
            booking.setCars(cars);
        }

        // Проверка и обновление дат
        LocalDate newStartDate = bookingDetails.getStartDate() != null
                ? bookingDetails.getStartDate() : booking.getStartDate();
        LocalDate newEndDate = bookingDetails.getEndDate() != null
                ? bookingDetails.getEndDate() : booking.getEndDate();

        if (!newStartDate.equals(booking.getStartDate())
                || !newEndDate.equals(booking.getEndDate())) {
            checkCarAvailability(carsToCheck, newStartDate, newEndDate, id);
            booking.setStartDate(newStartDate);
            booking.setEndDate(newEndDate);
        }

        // Сохраняем обновления
        Booking updatedBooking = bookingRepository.save(booking);
        bookingCache.put(updatedBooking.getId(), updatedBooking);
        return updatedBooking;
    }

    private void checkCarAvailability(List<Long> carIds, LocalDate startDate, LocalDate endDate) {
        checkCarAvailability(carIds, startDate, endDate, null);
    }

    private void checkCarAvailability(List<Long> carIds, LocalDate startDate,
                                      LocalDate endDate, Long excludeBookingId) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Дата окончания должна быть после даты начала");
        }

        List<Car> cars = carRepository.findAllById(carIds);
        for (Car car : cars) {
            List<Booking> overlappingBookings = bookingRepository.findByCarAndDateRange(
                    car.getId(), startDate, endDate, excludeBookingId);

            if (!overlappingBookings.isEmpty()) {
                throw new IllegalArgumentException(
                        "Машина с ID " + car.getId() + " уже забронирована на указанные даты");
            }
        }
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
