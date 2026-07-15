package com.yashwanth.bookNstay.controller;

import com.yashwanth.bookNstay.dto.BookingDto;
import com.yashwanth.bookNstay.dto.BookingRequest;
import com.yashwanth.bookNstay.dto.GuestDto;
import com.yashwanth.bookNstay.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/init")
    public ResponseEntity<BookingDto> initializeBooking(@RequestBody BookingRequest bookingRequest) {
        return new ResponseEntity<>(bookingService.initializeBooking(bookingRequest), HttpStatus.CREATED);
    }

    @PostMapping("{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuests(@PathVariable("bookingId") Long booingId, @RequestBody List<GuestDto> guestDtoList) {
        return new ResponseEntity<>(bookingService.addguests(booingId, guestDtoList), HttpStatus.CREATED);
    }

    @PostMapping("{bookingId}/payments")
    public ResponseEntity<Map<String,String>> initiatePayment(@PathVariable("bookingId") Long bookingId) {
        String sessionUrl = bookingService.initiatePayment(bookingId);
        return ResponseEntity.ok(Map.of("sessionUrl", sessionUrl));
    }

    @PostMapping("{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable("bookingId") Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{bookingId}/status")
    public ResponseEntity<Map<String,String>> getBookingStatus(@PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok( Map.of("Booking Status: ", bookingService.getBookingStatus(bookingId)));
    }

}
