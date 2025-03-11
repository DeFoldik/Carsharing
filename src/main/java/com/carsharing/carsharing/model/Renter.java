package com.carsharing.carsharing.model;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("RENTER")
public class Renter extends User {
    public Renter() {}

    public Renter(String name) {
        super(name);
    }
}