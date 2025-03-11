package com.carsharing.carsharing.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "renter_id")
    @NotNull(message = "Renter is required") // Валидация: арендатор не может быть null
    private Renter renter;

    @ManyToOne
    @JoinColumn(name = "car_id")
    @NotNull(message = "Car is required") // Валидация: машина не может быть null
    private Car car;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future") // Дата начала должна быть в будущем
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future") // Дата окончания должна быть в будущем
    private LocalDate endDate;

    public Booking() {}

    public Booking(Renter renter, Car car, LocalDate startDate, LocalDate endDate) {
        this.renter = renter;
        this.car = car;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public Renter getRenter() {
        return renter;
    }

    public Car getCar() {
        return car;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    // Сеттеры (чтобы обновлять данные)
    public void setRenter(Renter renter) {
        this.renter = renter;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}