package com.carsharing.carsharing.service;

import com.carsharing.carsharing.cache.UserCache;
import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Renter;
import com.carsharing.carsharing.repository.BookingRepository;
import com.carsharing.carsharing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RenterServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserCache userCache;

    @InjectMocks private RenterService renterService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllRenters_shouldReturnList() {
        List<Renter> renters = List.of(mock(Renter.class), mock(Renter.class));
        when(userRepository.findAllRenters()).thenReturn(renters);

        List<Renter> result = renterService.getAllRenters();

        assertThat(result).isEqualTo(renters);
    }

    @Test
    void getRenterById_shouldReturnFromCache() {
        Renter cached = mock(Renter.class);
        when(userCache.get(1L)).thenReturn(cached);

        Renter result = renterService.getRenterById(1L);

        assertThat(result).isEqualTo(cached);
        verify(userRepository, never()).findRenterById(anyLong());
    }

    @Test
    void getRenterById_shouldFetchFromRepoAndCache() {
        Renter renter = mock(Renter.class);
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findRenterById(1L)).thenReturn(Optional.of(renter));

        Renter result = renterService.getRenterById(1L);

        assertThat(result).isEqualTo(renter);
        verify(userCache).put(1L, renter);
    }

    @Test
    void getRenterById_shouldThrowWhenNotFound() {
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findRenterById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> renterService.getRenterById(1L))
                .isInstanceOf(NotFound.class)
                .hasMessageContaining("Renter not found with ID: 1");
    }

    @Test
    void createRenter_shouldSaveAndCache() {
        Renter renter = mock(Renter.class);
        Renter saved = mock(Renter.class);
        when(saved.getId()).thenReturn(1L);

        when(userRepository.save(renter)).thenReturn(saved);

        Renter result = renterService.createRenter(renter);

        assertThat(result).isEqualTo(saved);
        verify(userCache).put(1L, saved);
    }

    @Test
    void deleteRenter_shouldDeleteBookingsAndRenter() {
        // Мокируем объекты
        Renter renter = mock(Renter.class);
        Booking booking1 = mock(Booking.class);
        Booking booking2 = mock(Booking.class);
        Car car1 = mock(Car.class);
        Car car2 = mock(Car.class);

        // Мокируем репозитории и кэш
        when(userCache.get(1L)).thenReturn(renter);
        when(bookingRepository.findByRenter(renter)).thenReturn(List.of(booking1, booking2));

        // Мокируем поведение getCars() и getBookings()
        List<Car> cars1 = new LinkedList<>();
        List<Car> cars2 = new LinkedList<>();
        cars1.add(car1);
        cars2.add(car2);

        when(booking1.getCars()).thenReturn(cars1);
        when(booking2.getCars()).thenReturn(cars2);

        // Мокируем getBookings() для автомобилей
        LinkedList<Booking> bookings1 = mock(LinkedList.class);
        LinkedList<Booking> bookings2 = mock(LinkedList.class);

        when(car1.getBookings()).thenReturn(bookings1);
        when(car2.getBookings()).thenReturn(bookings2);

        // Вызов метода
        renterService.deleteRenter(1L);

        // Проверка: убеждаемся, что remove был вызван для правильных объектов
        verify(bookings1).remove(booking1);
        verify(bookings2).remove(booking2);

        // Проверка: убедимся, что бронирования были удалены
        verify(bookingRepository).delete(booking1);
        verify(bookingRepository).delete(booking2);

        // Проверка на удаление арендатора и кэширование
        verify(userCache).remove(1L);
        verify(userRepository).delete(renter);
    }

    @Test
    void deleteRenter_shouldThrowIfNotFound() {
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> renterService.deleteRenter(1L))
                .isInstanceOf(NotFound.class)
                .hasMessageContaining("Renter not found with ID: 1");
    }

    @Test
    void updateRenter_shouldUpdateAndCache() {
        Renter existing = mock(Renter.class);
        Renter updates = new Renter();
        updates.setName("Updated Name");

        when(userCache.get(1L)).thenReturn(existing);
        when(existing.getId()).thenReturn(1L);
        when(userRepository.save(existing)).thenReturn(existing);

        Renter result = renterService.updateRenter(1L, updates);

        verify(existing).setName("Updated Name");
        verify(userCache).put(1L, existing);
        assertThat(result).isEqualTo(existing);
    }

    @Test
    void updateRenter_shouldThrowIfNotFound() {
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> renterService.updateRenter(1L, new Renter()))
                .isInstanceOf(NotFound.class)
                .hasMessageContaining("Renter not found with ID: 1");
    }
}
