package com.carsharing.carsharing.controller;

import com.carsharing.carsharing.model.Renter;
import com.carsharing.carsharing.service.RenterService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/renters")
public class RenterController {
    private final RenterService renterService;

    public RenterController(RenterService renterService) {
        this.renterService = renterService;
    }

    @GetMapping
    public List<Renter> getRenters() {
        return renterService.getAllRenters();
    }

    @GetMapping("/{id}")
    public Renter getRenterById(@PathVariable Long id) {
        return renterService.getRenterById(id);
    }

    @PostMapping
    public Renter createRenter(@RequestBody Renter renter) {
        return renterService.createRenter(renter);
    }

    @DeleteMapping("/{id}")
    public void deleteRenter(@PathVariable Long id) {
        renterService.deleteRenter(id);
    }
}

