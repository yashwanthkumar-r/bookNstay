package com.yashwanth.bookNstay.service.impl;

import com.yashwanth.bookNstay.Exception.ResourceNotFoundException;
import com.yashwanth.bookNstay.Exception.UnAuthorizedException;
import com.yashwanth.bookNstay.dto.BookingDto;
import com.yashwanth.bookNstay.dto.BookingRequest;
import com.yashwanth.bookNstay.dto.GuestDto;
import com.yashwanth.bookNstay.entity.*;
import com.yashwanth.bookNstay.entity.enums.BookingStatus;
import com.yashwanth.bookNstay.repository.*;
import com.yashwanth.bookNstay.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final GuestRepository guestRepository;


    @Override
    @Transactional
    public BookingDto initializeBooking(BookingRequest bookingRequest) {

        log.info("Initialize booking for hotels : {}, room: {},date {} - {}", bookingRequest.getHotelId()
                , bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + bookingRequest.getRoomId()));

        List<Inventory> inventories = inventoryRepository.findAndLockAvailableInventory(bookingRequest.getRoomId(),
                bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        long dateCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()) + 1;

        if (inventories.size() != dateCount) {
            throw new IllegalStateException("Room is not available anymore");
        }

        // Reserve the room -> update the booked count of inventories

        for (Inventory inventory : inventories) {
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequest.getRoomsCount());
        }

        inventoryRepository.saveAll(inventories);

        //Create the booking

        //TODO: calculate dynamic pricing

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(BigDecimal.TEN)
                .build();

        //booking = bookingRepository.save(booking);
        return modelMapper.map(bookingRepository.save(booking), BookingDto.class);

    }

    @Override
    @Transactional
    public BookingDto addguests(Long bookingId, List<GuestDto> guestDtoList) {

        log.info("Adding guests for booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        User user = getCurrentUser();

        if(!user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking does not belong to the user with id:" + user.getId());
        }

        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }

        if (booking.getBookingStatus() != BookingStatus.RESERVED) {
            throw new IllegalStateException("rooms are not in reserved State");
        }

        for (GuestDto guestDto : guestDtoList) {
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        return modelMapper.map(bookingRepository.save(booking), BookingDto.class);
    }

    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


}
