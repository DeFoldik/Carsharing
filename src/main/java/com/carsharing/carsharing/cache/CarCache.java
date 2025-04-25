package com.carsharing.carsharing.cache;

import com.carsharing.carsharing.model.Car;
import org.springframework.stereotype.Component;

@Component
public class CarCache extends LruCache<Long, Car> {

    public CarCache() {
        super(100); // Максимальная вместимость кэша (100 автомобилей)
    }

}
