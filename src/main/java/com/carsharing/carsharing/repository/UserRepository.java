package com.carsharing.carsharing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.carsharing.carsharing.model.User;

public interface UserRepository extends JpaRepository<User, String> {
}
