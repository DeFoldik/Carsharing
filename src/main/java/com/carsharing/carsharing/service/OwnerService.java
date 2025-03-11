package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.Owner;
import com.carsharing.carsharing.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OwnerService {
    private final UserRepository userRepository;

    public OwnerService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<Owner> getAllOwners() {
        return userRepository.findAllOwners();
    }

    public Owner getOwnerById(Long id) {
        return userRepository.findOwnerById(id)
                .orElseThrow(() -> new NotFound("Owner not found with ID: " + id));
    }

    public Owner createOwner(Owner owner) {
        return userRepository.save(owner);
    }

    public void deleteOwner(Long id) {
        userRepository.deleteById(id);
    }
}

