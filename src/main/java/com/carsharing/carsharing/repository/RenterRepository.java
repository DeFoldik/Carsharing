package  com.carsharing.carsharing.repository;

import com.carsharing.carsharing.model.Renter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RenterRepository extends JpaRepository<Renter, Long> {
}
