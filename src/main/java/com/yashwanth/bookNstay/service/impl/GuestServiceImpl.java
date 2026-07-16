package com.yashwanth.bookNstay.service.impl;

import com.stripe.model.PaymentIntent;
import com.yashwanth.bookNstay.Exception.ResourceNotFoundException;
import com.yashwanth.bookNstay.Exception.UnAuthorizedException;
import com.yashwanth.bookNstay.dto.GuestDto;
import com.yashwanth.bookNstay.entity.Booking;
import com.yashwanth.bookNstay.entity.Guest;
import com.yashwanth.bookNstay.entity.User;
import com.yashwanth.bookNstay.repository.BookingRepository;
import com.yashwanth.bookNstay.repository.GuestRepository;
import com.yashwanth.bookNstay.service.GuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.yashwanth.bookNstay.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestServiceImpl implements GuestService{

    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;

    @Override
    public GuestDto getGuestById(Long guestId) {
        log.info("Getting the guest for id: {}", guestId);
        User user = getCurrentUser();
        Guest guest = guestRepository.findById(guestId).orElseThrow(()->
                new ResourceNotFoundException("Guest not found with Id: "+ guestId));
        if(!user.equals(guest.getUser())){
            throw new UnAuthorizedException("You are not authorized to access this guest");
        }
        return modelMapper.map(guest, GuestDto.class);
    }

    @Override
    public List<GuestDto> getAllGuestsByUser() {
        log.info("Getting the all the guests for the current User");
        User user = getCurrentUser();
        return guestRepository.findByUser(user).stream()
                .map(guest -> modelMapper.map(guest,GuestDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GuestDto updateGuest(Long guestId, GuestDto guestDto){
        log.info("Update guests for the current User");

        User user = getCurrentUser();
        Guest guest = guestRepository.findById(guestId).orElseThrow(()->
                new ResourceNotFoundException("Guest not found with Id: "+ guestId));

        if(!user.equals(guest.getUser())){
            throw new UnAuthorizedException("You are not authorized to access this guest");
        }

        modelMapper.map(guestDto, guest);
        guest.setId(guestId);

        return modelMapper.map(guestRepository.save(guest), GuestDto.class);
    }

    @Override
    @Transactional
    public void deleteGuestById(Long guestId, Long bookingId) {
        log.info("Delete guests for the current User");

        User user = getCurrentUser();
        Guest guest = guestRepository.findById(guestId).orElseThrow(()->
                new ResourceNotFoundException("Guest not found with Id: "+ guestId));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));


        if(!user.equals(guest.getUser()) || !user.equals(booking.getUser())){
            throw new UnAuthorizedException("You are not authorized to access this guest");
        }

        if(!booking.getGuests().contains(guest)){
            throw new IllegalArgumentException("Guest does not belong to the specified booking");
        }

        booking.getGuests().remove(guest);
        guestRepository.deleteById(guestId);
    }
}
