package com.carsharing.carsharing.repository;

import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Renter;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByRenter(Renter renter);

    List<Booking> findByCars(Car car);


    @Query("SELECT b FROM Booking b JOIN b.cars c WHERE c.id = :carId "
            + "AND ((b.startDate BETWEEN :startDate AND :endDate) "
            + "OR (b.endDate BETWEEN :startDate AND :endDate) "
            + "OR (:startDate BETWEEN b.startDate AND b.endDate) "
            + "OR (:endDate BETWEEN b.startDate AND b.endDate)) "
            + "AND (:excludeBookingId IS NULL OR b.id != :excludeBookingId)")
    List<Booking> findByCarAndDateRange(
            @Param("carId") Long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeBookingId") Long excludeBookingId);

    //List<Booking> findByOwnerUsername(String username);
}
