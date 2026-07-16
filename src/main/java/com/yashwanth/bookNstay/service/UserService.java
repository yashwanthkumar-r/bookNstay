package com.yashwanth.bookNstay.service;

import com.yashwanth.bookNstay.dto.BookingDto;
import com.yashwanth.bookNstay.dto.ProfileUpdateRequestDto;
import com.yashwanth.bookNstay.dto.UserDto;
import com.yashwanth.bookNstay.entity.User;

import java.util.List;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto dto);

    List<BookingDto> getMyBookings();

    UserDto getMyProfile();
}
