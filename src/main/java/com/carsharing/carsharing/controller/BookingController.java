package com.carsharing.carsharing.controller;

import com.carsharing.carsharing.model.Booking;
import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }


    @PreAuthorize("hasRole('RENTER')")
    @PostMapping
    public Booking createBooking(@RequestParam Long renterId, @RequestParam List<Long> carIds,
                                 @RequestBody @Valid Booking booking) {
        return bookingService.createBooking(renterId, carIds, booking);
    }
/*
    @PreAuthorize("hasRole('RENTER')")
    @PostMapping
    public Booking createBooking(@RequestParam List<Long> carIds,
                                 @RequestBody @Valid Booking booking,
                                 Authentication authentication) {
        String username = authentication.getName();
        return bookingService.createBooking(username, carIds, booking);
    }*/


    @PreAuthorize("hasRole('RENTER')")
    @PutMapping("/{id}")
    public Booking updateBooking(@PathVariable Long id,
                                 @RequestParam(required = true) Long renterId,
                                 @RequestParam(required = true) List<Long> carIds,
                                 @RequestBody @Valid Booking bookingDetails) {
        return bookingService.updateBooking(id, renterId, carIds, bookingDetails);
    }

    @PreAuthorize("hasRole('RENTER')")
    @GetMapping("/my")
    public ResponseEntity<List<Booking>> getMyBookings(Authentication authentication) {
        String username = authentication.getName();
        List<Booking> bookings = bookingService.getBookingsForRenter(username);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public Booking getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    /*@PreAuthorize("hasRole('RENTER')")
    @GetMapping("/my")
    public List<Booking> getMyBookings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return bookingService.getBookingsForRenter(username);
    }*/

    @PreAuthorize("hasRole('RENTER')")
    @DeleteMapping("/{id}")
    public void deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/owner")
    public List<Booking> getBookingsForOwner() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return bookingService.getBookingsForOwner(username);
    }

}
