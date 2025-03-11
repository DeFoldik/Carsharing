package com.carsharing.carsharing.repository;

import com.carsharing.carsharing.model.Owner;
import com.carsharing.carsharing.model.Renter;
import com.carsharing.carsharing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Получить всех рентеров
    @Query("SELECT u FROM User u WHERE TYPE(u) = Renter")
    List<Renter> findAllRenters();

    // Получить всех владельцев
    @Query("SELECT u FROM User u WHERE TYPE(u) = Owner")
    List<Owner> findAllOwners();

    // Получить рентера по ID
    @Query("SELECT u FROM User u WHERE TYPE(u) = Renter AND u.id = :id")
    Optional<Renter> findRenterById(@Param("id") Long id);

    // Получить владельца по ID
    @Query("SELECT u FROM User u WHERE TYPE(u) = Owner AND u.id = :id")
    Optional<Owner> findOwnerById(@Param("id") Long id);
}