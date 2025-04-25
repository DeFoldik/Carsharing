package com.carsharing.carsharing.cache;

import com.carsharing.carsharing.model.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingCache extends LruCache<Long, Booking> {

    public BookingCache() {
        super(100); // Максимальная вместимость кэша (100 автомобилей)
    }
}
