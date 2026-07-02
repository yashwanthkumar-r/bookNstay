package com.yashwanth.bookNstay.service.impl;

import com.yashwanth.bookNstay.Exception.ResourceNotFoundException;
import com.yashwanth.bookNstay.dto.HotelDto;
import com.yashwanth.bookNstay.dto.HotelInfoDto;
import com.yashwanth.bookNstay.dto.RoomDto;
import com.yashwanth.bookNstay.entity.Hotel;
import com.yashwanth.bookNstay.entity.Room;
import com.yashwanth.bookNstay.repository.HotelRepository;
import com.yashwanth.bookNstay.service.HotelService;
import com.yashwanth.bookNstay.service.InventoryService;
import com.yashwanth.bookNstay.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;
    private final RoomService roomService;

    @Override
    @Transactional
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating new Hotel with name: {}", hotelDto.getName());
        Hotel newHotel = modelMapper.map(hotelDto, Hotel.class);
        newHotel.setActive(false);

        if (hotelRepository.existsByName(hotelDto.getName())) {
            log.error("Hotel with this name already exists: {}", hotelDto.getName());
            throw new RuntimeException("Hotel with this name already exists");
        }

        newHotel = hotelRepository.save(newHotel);
        log.info("New Hotel created with Id: {}", newHotel.getId());

        return modelMapper.map(newHotel, HotelDto.class);

    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Fetch the hotel with id: {}", id);

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Hotel not found with id: {}", id);
                    return new ResourceNotFoundException("Hotel not found with id: " + id);
                });

        return modelMapper.map(hotel, HotelDto.class);

    }

    @Override
    public List<HotelDto> getAllHotels() {
        log.info("Fetching all the hotels...");

        List<Hotel> hotels = hotelRepository.findAll();

        return hotels.stream()
                .map(hotel -> modelMapper.map(hotel, HotelDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("updating the hotel with id: {}", id);

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Hotel not found with id: {}", id);
                    return new ResourceNotFoundException("Hotel not found with id: " + id);
                });

        modelMapper.map(hotelDto, hotel);
        hotel.setId(id);

        return modelMapper.map(hotelRepository.save(hotel), HotelDto.class);
    }


    @Override
    @Transactional
    public void activateHotel(Long id) {
        log.info("Set hotel status to active: {}", id);

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

        hotel.setActive(true);

        //Create inventory for all the rooms for this hotel
        //assuming only do it once
        for (Room room : hotel.getRooms()) {
            inventoryService.initializeRoomForAYear(room);
        }
    }


    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        log.info("deleting the hotel with id: {}", id);

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

        //delete the future inventories for this hotel
        for (Room room : hotel.getRooms()) {
            roomService.deleteRoomById(room.getId());
            //inventoryService.deleteAllInventories(room);
        }

        log.info("All rooms and inventory of this hotel with id: ,{} deleted", id);

        hotelRepository.deleteById(id);
        log.info("deleted the hotel with id: {}", id);

    }

    @Override
    public HotelInfoDto getHotelInfoByID(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));

        List<RoomDto> rooms = hotel.getRooms().stream()
                .map((room) -> modelMapper.map(room, RoomDto.class))
                .toList();

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);


    }

}
