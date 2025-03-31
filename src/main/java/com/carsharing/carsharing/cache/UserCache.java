package com.carsharing.carsharing.cache;

import com.carsharing.carsharing.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserCache extends LRUCache<Long, User> {

    public UserCache() {
        super(3); // Максимальная вместимость кэша (100 автомобилей)
    }

}