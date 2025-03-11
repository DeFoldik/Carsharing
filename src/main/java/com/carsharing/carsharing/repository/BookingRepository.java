package com.carsharing.carsharing.repository;

import com.carsharing.carsharing.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
