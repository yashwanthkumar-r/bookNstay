package com.yashwanth.bookNstay.service.impl;

import com.yashwanth.bookNstay.Exception.ResourceNotFoundException;
import com.yashwanth.bookNstay.Exception.UnAuthorizedException;
import com.yashwanth.bookNstay.dto.HotelPriceDto;
import com.yashwanth.bookNstay.dto.HotelSearchRequest;
import com.yashwanth.bookNstay.dto.InventoryDto;
import com.yashwanth.bookNstay.dto.UpdateInventoryRequestDto;
import com.yashwanth.bookNstay.entity.Inventory;
import com.yashwanth.bookNstay.entity.Room;
import com.yashwanth.bookNstay.entity.User;
import com.yashwanth.bookNstay.repository.HotelMinPriceRepository;
import com.yashwanth.bookNstay.repository.InventoryRepository;
import com.yashwanth.bookNstay.repository.RoomRepository;
import com.yashwanth.bookNstay.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.yashwanth.bookNstay.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final RoomRepository roomRepository;


    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);

        for (; !today.isAfter(endDate); today = today.plusDays(1)) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();

            inventoryRepository.save(inventory);

        }
    }

    @Override
    public void deleteAllInventories(Room room) {
        log.info("Deleting inventory of room with id: {}", room.getId());
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDto> searchHotel(HotelSearchRequest hotelSearchRequest) {
        log.info("Searching for hotels of {} city, from {} to {}",
                hotelSearchRequest.getCity(), hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate());
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());
        Long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate()) + 1;

        //business Logic - 90days
        Page<HotelPriceDto> hotelPage = hotelMinPriceRepository.findHotelsWithAvailableInventory(
                hotelSearchRequest.getCity(), hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate(), pageable);

      /*  Page<Hotel> hotelPage = inventoryRepository.findHotelsWithAvailableInventory(
                hotelSearchRequest.getCity(), hotelSearchRequest.getStartDate()
                , hotelSearchRequest.getEndDate(), hotelSearchRequest.getRoomsCount()
                , dateCount, pageable);*/

        //return hotelPage.map((element) -> modelMapper.map(element, HotelPriceDto.class));

        return hotelPage;
    }

    @Override
    public List<InventoryDto> getAllInventory(Long roomId) {
        log.info("get all the rooms with id: {}", roomId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found by Id: " + roomId));

        User user = getCurrentUser();

        if(!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorizedException("This is not the owner of the hotel, only owner can perform this action");
        }

        List<Inventory> inventories = inventoryRepository.findByRoomOrderByDate(room);

        return inventories.stream()
                .map(inventory -> modelMapper.map(inventory, InventoryDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        log.info("Updating all inventory by the rooms with id: {} between date range {} - {} ", roomId,
                updateInventoryRequestDto.getStartDate(), updateInventoryRequestDto.getEndDate());

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found by Id: " + roomId));

        User user = getCurrentUser();

        if(!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorizedException("This is not the owner of the hotel, only owner can perform this action");
        }

        inventoryRepository.getInventoryAndLockBeforeUpdate(roomId, updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate());

        inventoryRepository.updateInventory(roomId, updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate(), updateInventoryRequestDto.getClosed(),
                updateInventoryRequestDto.getSurgeFactor());

    }


}
