package com.yashwanth.bookNstay.controller;

import com.yashwanth.bookNstay.dto.HotelDto;
import com.yashwanth.bookNstay.dto.HotelInfoDto;
import com.yashwanth.bookNstay.dto.HotelSearchRequest;
import com.yashwanth.bookNstay.service.HotelService;
import com.yashwanth.bookNstay.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelDto>> searchHotel(@RequestBody HotelSearchRequest hotelSearchRequest) {
        Page<HotelDto> page = inventoryService.searchHotel(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelInfoByID(hotelId));
    }
}
