package com.yashwanth.bookNstay.repository;

import com.yashwanth.bookNstay.entity.Guest;
import com.yashwanth.bookNstay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByUser(User user);
}
