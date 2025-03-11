package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Renter;
import com.carsharing.carsharing.repository.RenterRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RenterService {
    private final RenterRepository renterRepository;

    public RenterService(RenterRepository renterRepository) {
        this.renterRepository = renterRepository;
    }

    public List<Renter> getAllRenters() {
        return renterRepository.findAll();
    }

    public Renter getRenterById(Long id) {
        return renterRepository.findById(id)
                .orElseThrow(() -> new NotFound("Renter not found with ID: " + id));
    }

    public Renter createRenter(Renter renter) {
        return renterRepository.save(renter);
    }

    public void deleteRenter(Long id) {
        renterRepository.deleteById(id);
    }
}

