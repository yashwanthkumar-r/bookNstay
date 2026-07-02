package com.yashwanth.bookNstay.service;

import com.yashwanth.bookNstay.dto.RoomDto;

import java.util.List;

public interface RoomService {

    RoomDto getRoomById(Long roomId);

    List<RoomDto> getAllRoomInHotel(Long hotelId);

    RoomDto createNewRoom(Long hotelId, RoomDto requestBody);

    RoomDto updateRoomById(Long roomId, RoomDto requestBody);

    void deleteRoomById(Long roomId);

}
