package com.yashwanth.bookNstay.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.yashwanth.bookNstay.Exception.ResourceNotFoundException;
import com.yashwanth.bookNstay.Exception.UnAuthorizedException;
import com.yashwanth.bookNstay.dto.BookingDto;
import com.yashwanth.bookNstay.dto.BookingRequest;
import com.yashwanth.bookNstay.dto.GuestDto;
import com.yashwanth.bookNstay.entity.*;
import com.yashwanth.bookNstay.entity.enums.BookingStatus;
import com.yashwanth.bookNstay.repository.*;
import com.yashwanth.bookNstay.service.BookingService;
import com.yashwanth.bookNstay.service.CheckoutService;
import com.yashwanth.bookNstay.strategy.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
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
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;


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
        inventoryRepository.initBooking(room.getId(), bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        //calculate dynamic pricing
        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventories);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));


        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(totalPrice)
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

    @Override
    @Transactional
    public String initiatePayment(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking not found with id: "+ bookingId)
        );

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking does not belong to the user with id:" + user.getId());
        }

        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking, frontendUrl+"/payment/success",
                frontendUrl+"/payment/failure" );

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType())){
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if(session == null){
                return;
            }
                String sessionId = session.getId();
                Booking booking =
                        bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(()->
                                new ResourceNotFoundException("session id not found in booking: "+ sessionId));
                booking.setBookingStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                        booking.getCheckOutDate(), booking.getRoomsCount());

                inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                        booking.getCheckOutDate(), booking.getRoomsCount());

                log.info("Booking successfully confirmed for session Id: {}",sessionId);
             }else {
                log.warn("Unhandled event type: {}", event.getType());
            }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking not found with id: "+ bookingId)
        );

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking does not belong to the user with id:" + user.getId());
        }

        if(booking.getBookingStatus()!=BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());

        //handle the refund
        try{
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund.create(refundParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking not found with id: "+ bookingId)
        );

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking does not belong to the user with id:" + user.getId());
        }

       return booking.getBookingStatus().name();
    }

    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


}
