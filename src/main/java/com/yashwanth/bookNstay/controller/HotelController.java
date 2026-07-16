package com.yashwanth.bookNstay.controller;

import com.yashwanth.bookNstay.dto.BookingDto;
import com.yashwanth.bookNstay.dto.HotelDto;
import com.yashwanth.bookNstay.dto.HotelReportDto;
import com.yashwanth.bookNstay.service.BookingService;
import com.yashwanth.bookNstay.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto) {
        return new ResponseEntity<>(hotelService.createNewHotel(hotelDto), HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable(name = "hotelId") Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable(name = "hotelId") Long id, @RequestBody HotelDto hotelDto) {
        return ResponseEntity.ok(hotelService.updateHotelById(id, hotelDto));
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable(name = "hotelId") Long id) {
        hotelService.deleteHotelById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}/activate")
    public ResponseEntity<Void> activateHotelById(@PathVariable(name = "hotelId") Long id) {
        hotelService.activateHotel(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{hotelId}/bookings")
    public ResponseEntity<List<BookingDto>> getAllBookingByHotelId(@PathVariable("hotelId") Long hotelId) {
        return ResponseEntity.ok(bookingService.getAllBookingByHotelId(hotelId));
    }

    @GetMapping("/{hotelId}/reports")
    public ResponseEntity<HotelReportDto> getReportByHotelId(@PathVariable("hotelId") Long hotelId,
                                                             @RequestParam(required = false)LocalDate startDate,
                                                             @RequestParam(required = false) LocalDate endDate) {
        if(startDate == null) startDate = LocalDate.now().minusMonths(1);
        if(endDate == null)   endDate = LocalDate.now();
        return ResponseEntity.ok(bookingService.getReportByHotelId(hotelId, startDate, endDate));
    }


}
