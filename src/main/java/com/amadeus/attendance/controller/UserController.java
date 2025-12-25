package com.amadeus.attendance.controller;

import com.amadeus.attendance.model.User;
import com.amadeus.attendance.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Get all users
    @GetMapping("/users")
    List<User> getUsers() {
        return userRepository.findAll();
    }

    // Get a specific user by ID
    @GetMapping("/user/{id}")
    ResponseEntity<?> getUser(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Create a new user
    @PostMapping("/user")
    ResponseEntity<User> createUser(@Valid @RequestBody User user) throws URISyntaxException {
        User result = userRepository.save(user);
        return ResponseEntity.created(new URI("/api/user/" + result.getId())).body(result);
    }

    // Update user (including goal percentage)
    @PutMapping("/user/{id}")
    ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        user.setId(id);
        User result = userRepository.save(user);
        return ResponseEntity.ok().body(result);
    }

    // Update only the WFO goal percentage
    @PatchMapping("/user/{id}/goal")
    ResponseEntity<User> updateUserGoal(@PathVariable Long id, @RequestParam Integer goalPercentage) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setWfoGoalPercentage(goalPercentage);
            User result = userRepository.save(user);
            return ResponseEntity.ok().body(result);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Delete a user
    @DeleteMapping("/user/{id}")
    ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}