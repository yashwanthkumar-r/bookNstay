package com.yashwanth.bookNstay.service.impl;

import com.yashwanth.bookNstay.Exception.ResourceNotFoundException;
import com.yashwanth.bookNstay.Exception.UnAuthorizedException;
import com.yashwanth.bookNstay.dto.RoomDto;
import com.yashwanth.bookNstay.entity.Hotel;
import com.yashwanth.bookNstay.entity.Room;
import com.yashwanth.bookNstay.entity.User;
import com.yashwanth.bookNstay.repository.HotelRepository;
import com.yashwanth.bookNstay.repository.RoomRepository;
import com.yashwanth.bookNstay.service.InventoryService;
import com.yashwanth.bookNstay.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;

    @Override
    public RoomDto getRoomById(Long id) {
        log.info("fetching room by id: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found by Id: " + id));

        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomInHotel(Long hotelId) {
        log.info("Getting all the rooms from the hotel");

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> {
                    log.error("Hotel not found with id: {}", hotelId);
                    return new ResourceNotFoundException("Hotel not found with id: " + hotelId);
                });

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This is not the owner of the hotel");
        }


        return hotel.getRooms().stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public RoomDto createNewRoom(Long hotelId, RoomDto dto) {
        log.info("creating new room...");

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> {
                    log.error("Hotel not found with id: {}", hotelId);
                    return new ResourceNotFoundException("Hotel not found with id: " + hotelId);
                });
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This is not the owner of the hotel");
        }

        Room room = modelMapper.map(dto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        //TODO: create inventory as soon as room is created and if hotel is active
        if (hotel.getActive()) {
            inventoryService.initializeRoomForAYear(room);
        }
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    @Transactional
    public RoomDto updateRoomById(Long id, RoomDto dto) {
        log.info("updating the room by id: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found by Id: " + id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorizedException("This is not the owner of the hotel, only owner can perform this action");
        }

        modelMapper.map(dto, room);
        room.setId(id);

        return modelMapper.map(roomRepository.save(room), RoomDto.class);

    }

    @Override
    @Transactional
    public void deleteRoomById(Long id) {
        log.info("deleting the room with id: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found by Id: " + id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorizedException("This is not the owner of the hotel, only owner can perform this action");
        }

        //TODO: delete all future inventory for this room
        inventoryService.deleteAllInventories(room);
        log.info("Room with id ,{} deleted in inventory", id);

        roomRepository.deleteById(id);
        log.info("deleted the room with id: {}", id);


    }
}
