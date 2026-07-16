package com.yashwanth.bookNstay.service;

import com.stripe.model.Event;
import com.yashwanth.bookNstay.dto.BookingDto;
import com.yashwanth.bookNstay.dto.BookingRequest;
import com.yashwanth.bookNstay.dto.GuestDto;
import com.yashwanth.bookNstay.dto.HotelReportDto;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {


    BookingDto initializeBooking(BookingRequest bookingRequest);

    BookingDto addguests(Long booingId, List<GuestDto> guestDtoList);

    String initiatePayment(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingByHotelId(Long hotelId);

    HotelReportDto getReportByHotelId(Long hotelId, LocalDate startDate, LocalDate endDate);
}
