package com.carsharing.carsharing.service;

import com.carsharing.carsharing.cache.UserCache;
import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Owner;
import com.carsharing.carsharing.repository.BookingRepository;
import com.carsharing.carsharing.repository.CarRepository;
import com.carsharing.carsharing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OwnerServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CarRepository carRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserCache userCache;

    @InjectMocks private OwnerService ownerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllOwners_shouldReturnList() {
        List<Owner> owners = List.of(mock(Owner.class), mock(Owner.class));
        when(userRepository.findAllOwners()).thenReturn(owners);

        List<Owner> result = ownerService.getAllOwners();

        assertThat(result).isEqualTo(owners);
    }

    @Test
    void getOwnerById_shouldReturnFromCache() {
        Owner cached = mock(Owner.class);
        when(userCache.get(1L)).thenReturn(cached);

        Owner result = ownerService.getOwnerById(1L);

        assertThat(result).isEqualTo(cached);
        verify(userRepository, never()).findOwnerById(anyLong());
    }

    @Test
    void getOwnerById_shouldFetchFromRepoAndCache() {
        Owner fetched = mock(Owner.class);
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findOwnerById(1L)).thenReturn(Optional.of(fetched));

        Owner result = ownerService.getOwnerById(1L);

        assertThat(result).isEqualTo(fetched);
        verify(userCache).put(1L, fetched);
    }

    @Test
    void getOwnerById_shouldThrowWhenNotFound() {
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findOwnerById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.getOwnerById(1L))
                .isInstanceOf(NotFound.class)
                .hasMessageContaining("Owner not found with ID: 1");
    }

    @Test
    void createOwner_shouldSaveAndCacheOwner() {
        Owner input = mock(Owner.class);
        Owner saved = mock(Owner.class);
        when(saved.getId()).thenReturn(1L);

        when(userRepository.save(input)).thenReturn(saved);

        Owner result = ownerService.createOwner(input);

        assertThat(result).isEqualTo(saved);
        verify(userCache).put(1L, saved);
    }

    @Test
    void deleteOwner_shouldOnlyDeleteOwnerIfNoCars() {
        Owner owner = mock(Owner.class);
        when(userCache.get(1L)).thenReturn(owner);
        when(carRepository.findByOwner(owner)).thenReturn(Collections.emptyList());

        ownerService.deleteOwner(1L);

        verify(userRepository).delete(owner);
    }

    @Test
    void deleteOwner_shouldDeleteCarsAndBookingsAndOwner() {
        Owner owner = mock(Owner.class);
        Car car1 = mock(Car.class);
        Car car2 = mock(Car.class);
        Booking booking1 = mock(Booking.class);
        Booking booking2 = mock(Booking.class);

        when(userCache.get(1L)).thenReturn(owner);
        when(carRepository.findByOwner(owner)).thenReturn(List.of(car1, car2));
        when(bookingRepository.findByCars(car1)).thenReturn(List.of(booking1));
        when(bookingRepository.findByCars(car2)).thenReturn(List.of(booking2));

        ownerService.deleteOwner(1L);

        verify(bookingRepository).deleteAll(List.of(booking1));
        verify(bookingRepository).deleteAll(List.of(booking2));
        verify(carRepository).deleteAll(List.of(car1, car2));
        verify(userCache).remove(1L);
        verify(userRepository).delete(owner);
    }

    @Test
    void updateOwner_shouldUpdateAndCache() {
        Owner existing = mock(Owner.class);
        Owner updates = new Owner();
        updates.setName("New Name");

        when(userCache.get(1L)).thenReturn(existing);
        when(existing.getId()).thenReturn(1L);
        when(userRepository.save(existing)).thenReturn(existing);

        Owner result = ownerService.updateOwner(1L, updates);

        verify(existing).setName("New Name");
        verify(userCache).put(1L, existing);
        assertThat(result).isEqualTo(existing);
    }

    @Test
    void updateOwner_shouldThrowIfNotFound() {
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.updateOwner(1L, new Owner()))
                .isInstanceOf(NotFound.class)
                .hasMessageContaining("Owner not found with ID: 1");
    }
}
