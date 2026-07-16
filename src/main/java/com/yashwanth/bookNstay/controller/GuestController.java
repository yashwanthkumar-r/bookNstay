package com.yashwanth.bookNstay.controller;

import com.yashwanth.bookNstay.dto.GuestDto;
import com.yashwanth.bookNstay.service.GuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users/guests")
@RequiredArgsConstructor
@Slf4j
public class GuestController {

    private final GuestService guestService;

    @GetMapping("/{guestId}")
    public ResponseEntity<GuestDto> getGuestById(@PathVariable(name = "guestId") Long guestId){
        return ResponseEntity.ok(guestService.getGuestById(guestId));
    }

    @GetMapping()
    public ResponseEntity<List<GuestDto>> getAllGuestsByUser(){
        return ResponseEntity.ok(guestService.getAllGuestsByUser());
    }

    @PutMapping("/{guestId}")
    public ResponseEntity<GuestDto> updateGuestById(@PathVariable(name = "guestId") Long guestId,
                                                 @RequestBody GuestDto guestDto){
        return ResponseEntity.ok(guestService.updateGuest(guestId, guestDto));
    }

    @DeleteMapping("/{guestId}/bookings/{bookingId}")
    public ResponseEntity<Void> deleteGuestById(@PathVariable(name = "guestId") Long guestId,
                                                @PathVariable(name = "bookingId") Long bookingId){
        guestService.deleteGuestById(guestId, bookingId);
        return ResponseEntity.noContent().build();
    }
}
