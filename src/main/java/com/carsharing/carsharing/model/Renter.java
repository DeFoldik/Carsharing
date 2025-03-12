package com.carsharing.carsharing.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("RENTER")
public class Renter extends User {
    public Renter() {}

    public Renter(String name) {
        super(name);
    }
}