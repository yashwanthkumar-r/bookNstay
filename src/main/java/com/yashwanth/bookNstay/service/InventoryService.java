package com.yashwanth.bookNstay.service;

import com.yashwanth.bookNstay.dto.HotelPriceDto;
import com.yashwanth.bookNstay.dto.HotelSearchRequest;
import com.yashwanth.bookNstay.dto.InventoryDto;
import com.yashwanth.bookNstay.dto.UpdateInventoryRequestDto;
import com.yashwanth.bookNstay.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotel(HotelSearchRequest hotelSearchRequest);

    List<InventoryDto> getAllInventory(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
