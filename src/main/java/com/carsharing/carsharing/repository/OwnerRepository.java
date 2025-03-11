package com.carsharing.carsharing.repository;

import com.carsharing.carsharing.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
}