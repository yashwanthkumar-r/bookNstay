package com.yashwanth.bookNstay.service;

import com.yashwanth.bookNstay.dto.GuestDto;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface GuestService {
    GuestDto getGuestById(Long guestId);

    List<GuestDto> getAllGuestsByUser();

    GuestDto updateGuest(Long guestId, GuestDto guestDto);

    void deleteGuestById(Long guestId, Long bookingId);
}
