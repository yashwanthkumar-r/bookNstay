package com.yashwanth.bookNstay.repository;

import com.yashwanth.bookNstay.entity.Hotel;
import com.yashwanth.bookNstay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    boolean existsByName(String name);

    List<Hotel> findByOwner(User user);
}