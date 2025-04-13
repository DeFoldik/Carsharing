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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private BookingCache bookingCache;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void getAllBookings_shouldReturnAllBookings() {
        // Создаем бронирования без установки ID напрямую
        Booking booking1 = new Booking();
        booking1.setStartDateString("01.01.2023");

        Booking booking2 = new Booking();
        booking2.setStartDateString("02.01.2023");

        List<Booking> bookings = List.of(booking1, booking2);

        when(bookingRepository.findAll()).thenReturn(bookings);

        List<Booking> result = bookingService.getAllBookings();

        assertThat(result).isEqualTo(bookings);
    }

    @Test
    void getBookingById_shouldReturnBookingFromCache() {
        Booking booking = new Booking();
        booking.setStartDateString("01.01.2023");

        when(bookingCache.getOrFetch(eq(1L), any(Function.class))).thenReturn(booking);

        Booking result = bookingService.getBookingById(1L);

        assertThat(result).isEqualTo(booking);
    }

    @Test
    void getBookingById_shouldFetchFromRepositoryWhenNotInCache() {
        // Arrange
        Long bookingId = 1L;
        Booking expectedBooking = new Booking();
        expectedBooking.setStartDateString("01.01.2023");
        expectedBooking.setEndDateString("05.01.2023");

        when(bookingCache.getOrFetch(eq(bookingId), any(Function.class)))
                .thenAnswer(invocation -> {
                    Function<Long, Booking> fetchFunction = invocation.getArgument(1);
                    return fetchFunction.apply(bookingId);
                });

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(expectedBooking));

        // Act
        Booking result = bookingService.getBookingById(bookingId);

        // Assert
        assertThat(result).isEqualTo(expectedBooking);
        verify(bookingRepository).findById(bookingId);
    }

    @Test
    void getBookingById_shouldThrowException_whenBookingNotFound() {
        when(bookingCache.getOrFetch(eq(99L), any(Function.class))).thenThrow(new NotFound("Booking not found with ID: 99"));

        assertThatThrownBy(() -> bookingService.getBookingById(99L))
                .isInstanceOf(NotFound.class)
                .hasMessage("Booking not found with ID: 99");
    }

    @Test
    void createBooking_shouldSaveAndReturnNewBooking() {
        // Given
        Long renterId = 1L;
        List<Long> carIds = List.of(1L, 2L);

        Booking booking = new Booking();
        booking.setStartDate(LocalDate.of(2023, 1, 1));
        booking.setEndDate(LocalDate.of(2023, 1, 5));

        Renter renter = new Renter();
        Car car1 = new Car();
        Car car2 = new Car();

        // When
        when(userRepository.findById(renterId)).thenReturn(Optional.of(renter));
        when(carRepository.findAllById(carIds)).thenReturn(List.of(car1, car2));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            return saved; // ID будет установлен Hibernate
        });

        Booking result = bookingService.createBooking(renterId, carIds, booking);

        // Then
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2023, 1, 5));
        assertThat(result.getCars()).containsExactly(car1, car2);
        verify(bookingCache).put(any(), eq(result));
    }

    @Test
    void updateBooking_shouldUpdateAndReturnBooking() {
        // Given
        Long id = 1L;
        Long renterId = 1L;
        List<Long> carIds = List.of(1L, 2L);

        Booking existingBooking = new Booking();
        existingBooking.setStartDate(LocalDate.of(2023, 1, 1));
        existingBooking.setEndDate(LocalDate.of(2023, 1, 5));
        try {
            Field idField = Booking.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(existingBooking, id);
        } catch (Exception e) {
            fail("Failed to set booking ID", e);
        }

        Booking updatedDetails = new Booking();
        updatedDetails.setStartDate(LocalDate.of(2023, 1, 10));
        updatedDetails.setEndDate(LocalDate.of(2023, 1, 15));

        Renter renter = new Renter();
        Car car1 = new Car();
        Car car2 = new Car();

        // When
        when(bookingCache.get(id)).thenReturn(existingBooking);
        when(userRepository.findById(renterId)).thenReturn(Optional.of(renter));
        when(carRepository.findAllById(carIds)).thenReturn(List.of(car1, car2));
        when(bookingRepository.save(existingBooking)).thenReturn(existingBooking);

        Booking result = bookingService.updateBooking(id, renterId, carIds, updatedDetails);
        // Then
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2023, 1, 10));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2023, 1, 15));
        verify(bookingCache).put(id, existingBooking);
    }

    @Test
    void updateBooking_shouldThrow_whenUserNotRenter() {
        // Given
        Long id = 1L;
        Long renterId = 2L;
        Booking existingBooking = new Booking();
        when(bookingCache.get(id)).thenReturn(existingBooking);

        // Not a renter
        when(userRepository.findById(renterId)).thenReturn(Optional.of(mock(User.class)));

        // Then
        assertThatThrownBy(() -> bookingService.updateBooking(id, renterId, null, new Booking()))
                .isInstanceOf(NotFound.class)
                .hasMessageContaining("is not a Renter");
    }

    @Test
    void updateBooking_shouldThrow_whenCarIdsProvidedButCarsNotFound() {
        // Given
        Long id = 1L;
        List<Long> carIds = List.of(1L, 2L);
        Booking existingBooking = new Booking();
        when(bookingCache.get(id)).thenReturn(existingBooking);

        when(carRepository.findAllById(carIds)).thenReturn(List.of()); // No cars found

        // Then
        assertThatThrownBy(() -> bookingService.updateBooking(id, null, carIds, new Booking()))
                .isInstanceOf(NotFound.class)
                .hasMessageContaining("Cars not found");
    }

    @Test
    void updateBooking_shouldUseExistingCarsWhenCarIdsIsNull() {
        // Given
        Long id = 1L;
        Car existingCar = new Car();
        Long carId = 42L;
        try {
            Field idField = Car.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(existingCar, carId);
        } catch (Exception e) {
            fail("Failed to set car ID", e);
        }

        Booking existingBooking = new Booking();
        existingBooking.setStartDate(LocalDate.of(2023, 1, 1));
        existingBooking.setEndDate(LocalDate.of(2023, 1, 5));
        existingBooking.setCars(List.of(existingCar));
        when(bookingCache.get(id)).thenReturn(existingBooking);
        when(bookingRepository.save(existingBooking)).thenReturn(existingBooking);

        // Dates changed => checkCarAvailability will be called
        Booking updatedDetails = new Booking();
        updatedDetails.setStartDate(LocalDate.of(2023, 1, 2));
        updatedDetails.setEndDate(LocalDate.of(2023, 1, 6));

        when(carRepository.findAllById(List.of(carId))).thenReturn(List.of(existingCar));
        when(bookingRepository.findByCarAndDateRange(eq(carId), any(), any(), eq(id)))
                .thenReturn(List.of());

        Booking result = bookingService.updateBooking(id, null, null, updatedDetails);

        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2023, 1, 2));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2023, 1, 6));
    }

    @Test
    void deleteBooking_shouldDeleteBooking() {
        // Given
        Long id = 1L;
        Booking booking = new Booking();
        booking.setStartDateString("01.01.2023");

        // When
        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));
        doNothing().when(bookingRepository).delete(booking);

        bookingService.deleteBooking(id);

        // Then
        verify(bookingRepository).delete(booking);
        verify(bookingCache).remove(id);
    }

    @Test
    void createBooking_shouldThrowWhenCarNotAvailable() {
        // Given
        Long renterId = 1L;
        Long carId = 1L;
        List<Long> carIds = List.of(carId);

        // Создаем тестовое бронирование
        Booking booking = new Booking();
        booking.setStartDateString("01.05.2025");
        booking.setEndDateString("05.05.2025");

        Renter renter = new Renter();

        Car car = new Car();
        try {
            Field idField = Car.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(car, carId);
        } catch (Exception e) {
            fail("Failed to set car ID", e);
        }

        // Создаем существующее бронирование, которое создаст конфликт
        Booking savedBookingWithParsedDates = new Booking();
        savedBookingWithParsedDates.setStartDate(LocalDate.of(2025, 5, 1));
        savedBookingWithParsedDates.setEndDate(LocalDate.of(2025, 5, 5));

        // Конфликтующее бронирование
        Booking conflictingBooking = new Booking();
        conflictingBooking.setStartDate(LocalDate.of(2025, 5, 3));
        conflictingBooking.setEndDate(LocalDate.of(2025, 5, 7));

        // When
        when(userRepository.findById(renterId)).thenReturn(Optional.of(renter));
        when(carRepository.findAllById(carIds)).thenReturn(List.of(car));

        // Первое сохранение (когда сервис вызывает save перед проверкой доступности)
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBookingWithParsedDates);

        // Проверка доступности
        when(bookingRepository.findByCarAndDateRange(
                eq(carId),
                eq(LocalDate.of(2025, 5, 1)),
                eq(LocalDate.of(2025, 5, 5)),
                isNull()))
                .thenReturn(List.of(conflictingBooking));

        // Then
        assertThatThrownBy(() -> bookingService.createBooking(renterId, carIds, booking))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("уже забронирована");
    }

    @Test
    void updateBooking_shouldNotCheckAvailabilityIfDatesNotChanged() {
        // Given
        Long id = 1L;
        Booking existingBooking = new Booking();
        existingBooking.setStartDateString("01.01.2023");
        existingBooking.setEndDateString("05.01.2023");

        // When
        when(bookingCache.get(id)).thenReturn(existingBooking);
        when(bookingRepository.save(existingBooking)).thenReturn(existingBooking);

        // Пытаемся обновить без изменения дат
        Booking updatedDetails = new Booking();
        updatedDetails.setStartDate(LocalDate.of(2023, 1, 1));
        updatedDetails.setEndDate(LocalDate.of(2023, 1, 5));

        Booking updated = bookingService.updateBooking(id, null, null, updatedDetails);

        // Then
        verify(bookingRepository, never()).findByCarAndDateRange(any(), any(), any(), any());
    }

    @Test
    void deleteBooking_shouldRemoveBookingFromCarAndClearCars() {
        // Given
        Long bookingId = 1L;
        Car car = new Car();
        Booking booking = new Booking();
        booking.setCars(new ArrayList<>(List.of(car)));
        car.setBookings(new ArrayList<>(List.of(booking)));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        bookingService.deleteBooking(bookingId);

        assertThat(booking.getCars()).isEmpty();
        verify(carRepository).saveAll(any());
        verify(bookingRepository).delete(booking);
        verify(bookingCache).remove(bookingId);
    }

    @Test
    void createBooking_shouldThrowIfCarsNotFound() {
        Long renterId = 1L;
        List<Long> carIds = List.of(100L, 101L);
        Booking booking = new Booking();
        booking.setStartDateString("01.01.2025");
        booking.setEndDateString("05.01.2025");

        when(userRepository.findById(renterId)).thenReturn(Optional.of(new Renter()));
        when(carRepository.findAllById(carIds)).thenReturn(List.of());

        assertThatThrownBy(() -> bookingService.createBooking(renterId, carIds, booking))
                .isInstanceOf(NotFound.class)
                .hasMessageContaining("One or more cars not found with provided IDs");
    }

    @Test
    void updateBooking_shouldKeepCarsIfCarIdsEmpty() {
        Long id = 1L;
        Car existingCar = new Car();
        Booking existingBooking = new Booking();
        existingBooking.setCars(List.of(existingCar));
        existingBooking.setStartDate(LocalDate.of(2023, 1, 1));
        existingBooking.setEndDate(LocalDate.of(2023, 1, 5));

        when(bookingCache.get(id)).thenReturn(existingBooking);
        when(bookingRepository.save(existingBooking)).thenReturn(existingBooking);

        Booking update = new Booking();
        update.setStartDate(LocalDate.of(2023, 1, 1));
        update.setEndDate(LocalDate.of(2023, 1, 5));

        Booking result = bookingService.updateBooking(id, null, List.of(), update);

        assertThat(result.getCars()).isEqualTo(List.of(existingCar));
    }

    @Test
    void deleteBooking_shouldThrowIfNotFound() {
        Long bookingId = 999L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.deleteBooking(bookingId))
                .isInstanceOf(NotFound.class)
                .hasMessageContaining("Booking not found with ID: " + bookingId);
    }

    @Test
    void updateBooking_shouldThrowIfCarUnavailableInNewDateRange() {
        // Given
        Long bookingId = 1L;
        Long carId = 10L;
        Car car = new Car();
        try {
            Field idField = Car.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(car, carId);
        } catch (Exception e) {
            fail("Failed to set car ID", e);
        }

        Booking existingBooking = new Booking();
        existingBooking.setStartDate(LocalDate.of(2023, 1, 1));
        existingBooking.setEndDate(LocalDate.of(2023, 1, 5));
        existingBooking.setCars(List.of(car));

        Booking conflictingBooking = new Booking();
        conflictingBooking.setStartDate(LocalDate.of(2023, 1, 10));
        conflictingBooking.setEndDate(LocalDate.of(2023, 1, 15));

        when(bookingCache.get(bookingId)).thenReturn(existingBooking);
        when(carRepository.findAllById(any())).thenReturn(List.of(car));
        when(bookingRepository.findByCarAndDateRange(eq(carId), eq(LocalDate.of(2023, 1, 10)), eq(LocalDate.of(2023, 1, 15)), eq(bookingId)))
                .thenReturn(List.of(conflictingBooking));

        Booking updatedDetails = new Booking();
        updatedDetails.setStartDate(LocalDate.of(2023, 1, 10));
        updatedDetails.setEndDate(LocalDate.of(2023, 1, 15));

        // Then
        assertThatThrownBy(() -> bookingService.updateBooking(bookingId, null, null, updatedDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("уже забронирована");
    }

    @Test
    void getBookingById_shouldThrow_whenNotInCacheAndNotInDb() {
        Long id = 123L;
        when(bookingCache.getOrFetch(eq(id), any())).thenAnswer(invocation -> {
            Function<Long, Booking> fetch = invocation.getArgument(1);
            return fetch.apply(id);
        });

        when(bookingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(id))
                .isInstanceOf(NotFound.class)
                .hasMessageContaining("Booking not found with ID: " + id);
    }

    @Test
    void createBooking_shouldThrowIfRenterNotFound() {
        Long renterId = 5L;
        List<Long> carIds = List.of(1L);
        Booking booking = new Booking();
        booking.setStartDateString("01.01.2025");
        booking.setEndDateString("05.01.2025");

        when(userRepository.findById(renterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(renterId, carIds, booking))
                .isInstanceOf(NotFound.class)
                .hasMessageContaining("Renter not found with ID: " + renterId);
    }

}
