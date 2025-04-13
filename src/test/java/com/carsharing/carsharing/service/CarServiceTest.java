package com.carsharing.carsharing.service;

import com.carsharing.carsharing.cache.CarCache;
import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.*;
import com.carsharing.carsharing.repository.BookingRepository;
import com.carsharing.carsharing.repository.CarRepository;
import com.carsharing.carsharing.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CarCache carCache;

    @InjectMocks
    private CarService carService;

    @Test
    void getAllCars_shouldReturnAllCars() {
        // Given
        Car car1 = new Car();
        Car car2 = new Car();
        List<Car> cars = List.of(car1, car2);

        when(carRepository.findAll()).thenReturn(cars);

        // When
        List<Car> result = carService.getAllCars();

        // Then
        assertThat(result).hasSize(2).containsExactly(car1, car2);
    }

    @Test
    void getCarById_shouldReturnCarFromCache() {
        // Given
        Car cachedCar = new Car();
        when(carCache.get(1L)).thenReturn(cachedCar);

        // When
        Car result = carService.getCarById(1L);

        // Then
        assertThat(result).isSameAs(cachedCar);
        verify(carRepository, never()).findById(any());
    }

    @Test
    void getCarById_shouldFetchFromDbAndCache_whenNotInCache() {
        // Given
        Car dbCar = new Car();
        when(carCache.get(1L)).thenReturn(null);
        when(carRepository.findById(1L)).thenReturn(Optional.of(dbCar));

        // When
        Car result = carService.getCarById(1L);

        // Then
        assertThat(result).isSameAs(dbCar);
        verify(carCache).put(1L, dbCar);
    }

    @Test
    void getCarById_shouldThrow_whenCarNotFound() {
        // Given
        when(carCache.get(99L)).thenReturn(null);
        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> carService.getCarById(99L))
                .isInstanceOf(NotFound.class)
                .hasMessage("Car not found with ID: 99");
    }

    @Test
    void getCarsByBrand_shouldReturnCars() {
        // Given
        String brand = "Toyota";
        Car car = new Car();
        car.setBrand(brand);
        List<Car> cars = List.of(car);

        when(carRepository.findByBrandIgnoreCase(brand)).thenReturn(cars);

        // When
        List<Car> result = carService.getCarsByBrand(brand);

        // Then
        assertThat(result).hasSize(1).first().extracting(Car::getBrand).isEqualTo(brand);
    }

    @Test
    void createCars_shouldCreateAndCacheCars() {
        // Given
        Owner owner = new Owner();
        List<Car> cars = List.of(new Car(), new Car());

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(carRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<Car> result = carService.createCars(cars, 1L);

        // Then
        assertThat(result).hasSize(2);
        result.forEach(car -> assertThat(car.getOwner()).isSameAs(owner));
        verify(carCache, times(2)).put(any(), any());
    }

    @Test
    void createCars_shouldThrow_whenOwnerNotFound() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> carService.createCars(List.of(new Car()), 99L))
                .isInstanceOf(NotFound.class)
                .hasMessage("Owner not found with ID: 99");
    }

    @Test
    void createCars_shouldThrow_whenUserNotOwner() {
        // Given
        User user = new Renter(); // Not an Owner
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Then
        assertThatThrownBy(() -> carService.createCars(List.of(new Car()), 1L))
                .isInstanceOf(NotFound.class)
                .hasMessage("User with ID 1 is not an Owner");
    }

    @Test
    void updateCar_shouldUpdateAndCacheCar() {
        // Given
        Car existingCar = new Car();
        existingCar.setBrand("Old Brand");

        Car updatedDetails = new Car();
        updatedDetails.setBrand("New Brand");
        updatedDetails.setModel("New Model");

        when(carCache.get(1L)).thenReturn(existingCar);
        when(carRepository.save(existingCar)).thenReturn(existingCar);

        // When
        Car result = carService.updateCar(1L, updatedDetails);

        // Then
        assertThat(result.getBrand()).isEqualTo("New Brand");
        assertThat(result.getModel()).isEqualTo("New Model");
        verify(carCache).put(1L, existingCar);
    }

    @Test
    void updateCar_shouldFetchFromDb_whenNotInCache() {
        // Given
        Car dbCar = new Car();
        when(carCache.get(1L)).thenReturn(null);
        when(carRepository.findById(1L)).thenReturn(Optional.of(dbCar));
        when(carRepository.save(dbCar)).thenReturn(dbCar);

        // When
        carService.updateCar(1L, new Car());

        // Then
        verify(carCache).put(1L, dbCar);
    }

    @Test
    void deleteCar_shouldDeleteCarAndRelatedBookings() {
        // Given
        Car car = new Car();
        when(carCache.get(1L)).thenReturn(car);
        when(bookingRepository.findByCars(car)).thenReturn(List.of(new Booking(), new Booking()));

        // When
        carService.deleteCar(1L);

        // Then
        verify(bookingRepository, times(2)).delete(any());
        verify(carRepository).deleteById(1L);
        verify(carCache).remove(1L);
    }

    @Test
    void getCarsByModel_shouldReturnCars() {
        // Given
        String model = "Camry";
        Car car = new Car();
        car.setModel(model);
        List<Car> cars = List.of(car);

        when(carRepository.findByModel(model)).thenReturn(cars);

        // When
        List<Car> result = carService.getCarsByModel(model);

        // Then
        assertThat(result).hasSize(1).first().extracting(Car::getModel).isEqualTo(model);
    }

    @Test
    void getCarsByOwnerName_shouldReturnCars() {
        // Given
        String ownerName = "John";
        List<Car> cars = List.of(new Car());
        when(carRepository.findByOwnerName(ownerName)).thenReturn(cars);

        // When
        List<Car> result = carService.getCarsByOwnerName(ownerName);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getCarsByOwnerNameNative_shouldReturnCars() {
        // Given
        String ownerName = "John";
        List<Car> cars = List.of(new Car());
        when(carRepository.findByOwnerNameNative(ownerName)).thenReturn(cars);

        // When
        List<Car> result = carService.getCarsByOwnerNameNative(ownerName);

        // Then
        assertThat(result).hasSize(1);
    }
}