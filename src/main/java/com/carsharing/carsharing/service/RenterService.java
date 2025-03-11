package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Renter;
import com.carsharing.carsharing.repository.UserRepository;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RenterService {

    private final UserRepository userRepository;

    public RenterService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<Renter> getAllRenters() {
        return userRepository.findAllRenters();
    }

    public Renter getRenterById(Long id) {
        return userRepository.findRenterById(id)
                .orElseThrow(() -> new NotFound("Renter not found with ID: " + id));
    }

    public Renter createRenter(Renter renter) {
        return userRepository.save(renter);
    }

    public void deleteRenter(Long id) {
        userRepository.deleteById(id);
    }
}

