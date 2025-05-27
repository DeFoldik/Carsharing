package com.carsharing.carsharing.model;

import com.carsharing.carsharing.validation.ValidDateFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "renter_id")
    private Renter renter;

    //@JsonManagedReference("car-booking")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "car_booking",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "car_id")
    )
    //@JsonIgnore
    private List<Car> cars = new ArrayList<>();

    @NotBlank(message = "Дата начала обязательна")
    @ValidDateFormat
    private String startDateString;

    @NotBlank(message = "Дата окончания обязательна")
    @ValidDateFormat
    private String endDateString;

    @JsonIgnore
    private LocalDate startDate;

    @JsonIgnore
    private LocalDate endDate;

    @PrePersist
    @PreUpdate
    public void parseAndValidateDates() {
        if (startDateString == null || endDateString == null) {
            throw new IllegalArgumentException("Даты начала и окончания обязательны");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        try {
            this.startDate = LocalDate.parse(startDateString, formatter);
            this.endDate = LocalDate.parse(endDateString, formatter);

            if (startDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Дата начала не может быть в прошлом");
            }

            if (endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("Дата окончания должна быть после даты начала");
            }

            if (startDate.isAfter(LocalDate.now().plusYears(1))) {
                throw new IllegalArgumentException("Дата начала не"
                        + " может быть более чем на год вперед");
            }

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат даты. Используйте dd.MM.yyyy");
        }
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public Renter getRenter() {
        return renter;
    }

    public void setRenter(Renter renter) {
        this.renter = renter;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    public String getStartDateString() {
        return startDateString;
    }

    public void setStartDateString(String startDateString) {
        this.startDateString = startDateString;
    }

    public String getEndDateString() {
        return endDateString;
    }

    public void setEndDateString(String endDateString) {
        this.endDateString = endDateString;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    // Сеттеры для LocalDate (используются только внутри приложения)
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}