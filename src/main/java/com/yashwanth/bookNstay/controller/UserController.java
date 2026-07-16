package com.yashwanth.bookNstay.controller;

import com.yashwanth.bookNstay.dto.BookingDto;
import com.yashwanth.bookNstay.dto.ProfileUpdateRequestDto;
import com.yashwanth.bookNstay.dto.UserDto;
import com.yashwanth.bookNstay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDto dto){
        userService.updateProfile(dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/myBookings")
    public ResponseEntity<List<BookingDto>> getMyBookings(){
        return ResponseEntity.ok(userService.getMyBookings());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getMyProfile(){
        return ResponseEntity.ok( userService.getMyProfile());
    }
}
