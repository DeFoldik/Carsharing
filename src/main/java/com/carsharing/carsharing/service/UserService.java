package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.NotFound;
import com.carsharing.carsharing.model.User;
import com.carsharing.carsharing.repository.CarRepository;
import com.carsharing.carsharing.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFound("User not found with ID: " + id));
    }

    public User createUser(User user) {
        if (userRepository.findByLogin(user.getLogin()) != null) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует.");
        }
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public User updateUser(String id, User userDetails) {
        User user = getUserById(id);
        user.setName(userDetails.getName());
        user.setCars(userDetails.getCars());
        return userRepository.save(user);
    }
}
