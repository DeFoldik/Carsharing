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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

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

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        return bookingRepository.findAll().stream()
                .filter(booking -> booking.getRenter().getUsername().equals(currentUsername))
                .collect(Collectors.toList());
    }

    // Получение бронирования по ID
    public Booking getBookingById(Long id) {
        //return bookingRepository.findById(id)
        //.orElseThrow(() -> new NotFound("Booking not found with ID: " + id));

        Booking booking = bookingCache.getOrFetch(id, key -> bookingRepository.findById(key)
                .orElseThrow(() -> new NotFound("Booking not found with ID: " + key)));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!booking.getRenter().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("Вы можете просматривать только свои бронирования");
        }

        return booking;
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

    /*@Transactional
    public Booking createBooking(String renterUsername, List<Long> carIds, Booking booking) {
        // 1. Найти арендатора по username
        User user = userRepository.findByUsername(renterUsername)
                .orElseThrow(() -> new NotFound("User not found: " + renterUsername));

        if (!(user instanceof Renter renter)) {
            throw new IllegalArgumentException("User is not a renter");
        }

        // 2. Проверка автомобилей
        List<Car> cars = carRepository.findAllById(carIds);
        if (cars.isEmpty() || cars.size() != carIds.size()) {
            throw new NotFound("One or more cars not found with provided IDs");
        }

        // 3. Сохраняем бронь для получения ID
        Booking savedBooking = bookingRepository.save(booking);

        // 4. Проверка доступности
        checkCarAvailability(carIds, savedBooking.getStartDate(), savedBooking.getEndDate());

        // 5. Связываем сущности
        savedBooking.setRenter(renter);
        savedBooking.setCars(cars);

        // 6. Сохраняем
        Booking finalBooking = bookingRepository.save(savedBooking);
        bookingCache.put(finalBooking.getId(), finalBooking);
        return finalBooking;
    }*/
    @Transactional
    public Booking updateBooking(Long id, Long renterId, List<Long> carIds, Booking bookingDetails) {
        log.info("Update booking requested for ID: {}", id);
        // 1. Получаем существующее бронирование
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFound("Booking not found with ID: " + id));

        // 2. Проверка прав доступа
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!existingBooking.getRenter().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You can only modify your own bookings");
        }

        // 3. Обновление арендатора (если передан)
        if (renterId != null) {
            Renter renter = (Renter) userRepository.findById(renterId)
                    .orElseThrow(() -> new NotFound("Renter not found with ID: " + renterId));
            existingBooking.setRenter(renter);
        }

        // 4. Подготовка списка автомобилей для проверки
        List<Long> carsToCheck = carIds != null ? carIds :
                existingBooking.getCars().stream()
                        .map(Car::getId)
                        .collect(Collectors.toList());

        // 5. Обновление автомобилей (если переданы)
        if (carIds != null && !carIds.isEmpty()) {
            List<Car> cars = carRepository.findAllById(carIds);
            if (cars.size() != carIds.size()) {
                throw new NotFound("One or more cars not found with provided IDs");
            }
            existingBooking.setCars(cars);
        }

        // 6. Обновление дат (если переданы)
        if (bookingDetails.getStartDateString() != null && bookingDetails.getEndDateString() != null) {
            bookingDetails.parseAndValidateDates();
            checkCarAvailability(carsToCheck, bookingDetails.getStartDate(), bookingDetails.getEndDate(), id);

            existingBooking.setStartDateString(bookingDetails.getStartDateString());
            existingBooking.setEndDateString(bookingDetails.getEndDateString());
        }

        // 7. Сохранение обновленного бронирования
        Booking updatedBooking = bookingRepository.save(existingBooking);
        bookingCache.put(updatedBooking.getId(), updatedBooking);
        return updatedBooking;
    }

    public List<Booking> getBookingsForOwner(String ownerUsername) {
        List<Car> ownerCars = carRepository.findByOwner_Username(ownerUsername);

        return bookingRepository.findAll().stream()
                .filter(booking -> booking.getCars().stream()
                        .anyMatch(car -> car.getOwner().getUsername().equals(ownerUsername)))
                .collect(Collectors.toList());
    }

   /* public List<Booking> getBookingsByUsername(String username) {
        return bookingRepository.findByOwnerUsername(username);
    }*/
    private void checkCarAvailability(List<Long> carIds, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Даты начала и окончания обязательны");
        }
        checkCarAvailability(carIds, startDate, endDate, null);
    }

    public List<Booking> getBookingsForRenter(String renterUsername) {
        return bookingRepository.findAll().stream()
                .filter(booking -> booking.getRenter().getUsername().equals(renterUsername))
                .collect(Collectors.toList());
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

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!booking.getRenter().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("Вы можете удалять только свои брони");
        }

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
