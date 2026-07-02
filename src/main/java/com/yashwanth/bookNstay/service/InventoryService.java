package com.yashwanth.bookNstay.service;

import com.yashwanth.bookNstay.dto.HotelDto;
import com.yashwanth.bookNstay.dto.HotelSearchRequest;
import com.yashwanth.bookNstay.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelDto> searchHotel(HotelSearchRequest hotelSearchRequest);

}
