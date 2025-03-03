package com.carsharing.carsharing.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;

    @ManyToMany(mappedBy = "cars", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JsonManagedReference
    private Set<User> users = new HashSet<>();

    public Car(String id, String brand, String model) {
        this.brand = brand;
        this.model = model;
    }

    public Car() {
        // Пустой конструктор для JPA
    }

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

}
