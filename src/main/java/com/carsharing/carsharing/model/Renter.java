package com.carsharing.carsharing.model;

import jakarta.persistence.Entity;

@Entity
public class Renter extends User {
    public Renter() {}

    public Renter(String name) {
        super(name);
    }
}