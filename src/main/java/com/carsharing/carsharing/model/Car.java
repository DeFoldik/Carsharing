package com.carsharing.carsharing.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Car {
    @Id
    private String id;
    private String brand;
    private String model;

    @ManyToMany(mappedBy = "cars", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JsonManagedReference
    private Set<User> users = new HashSet<>();

    public Car(String id, String brand, String model) {
        this.id = id;
        this.brand = brand;
        this.model = model;
    }

    public Car() {
        // Пустой конструктор для JPA
    }

    public String getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public void setId(String id) {
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
