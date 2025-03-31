package com.carsharing.carsharing.repository;

import com.carsharing.carsharing.model.Car;
import com.carsharing.carsharing.model.Owner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByBrandIgnoreCase(String brand);

    List<Car> findByOwner(Owner owner);

    // Поиск по модели автомобиля (JPQL)
    @Query("SELECT DISTINCT c FROM Car c "
            + "LEFT JOIN FETCH c.owner "
            + "WHERE c.model = :model")
    List<Car> findByModel(@Param("model") String model);

    @Query("SELECT DISTINCT c FROM Car c " +
            "LEFT JOIN FETCH c.owner o " +
            "WHERE o.name = :ownerName")
    List<Car> findByOwnerName(@Param("ownerName") String ownerName);

    @Query(value = "SELECT c.* FROM car c " +
            "LEFT JOIN user o ON c.owner_id = o.id " +
            "WHERE o.name = :ownerName", nativeQuery = true)
    List<Car> findByOwnerNameNative(@Param("ownerName") String ownerName);

}