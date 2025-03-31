package com.carsharing.carsharing.repository;

import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Renter;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByRenter(Renter renter);

    List<Booking> findByCars(Car car);

    @Query(value = "SELECT b.* FROM booking b " +
            "JOIN  bk ON .id = rp.ride_id " +
            "JOIN users p ON rp.user_id = p.id " +
            "WHERE p.name = :passengerName", nativeQuery = true)
    List<Car> findByModelNative(String passengerName);

}
