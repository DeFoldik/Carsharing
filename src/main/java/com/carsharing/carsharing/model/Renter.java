package com.carsharing.carsharing.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Renter extends User {
    public Renter() {}

    public Renter(String name) {
        super(name);
    }
}