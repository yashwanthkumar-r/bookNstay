package com.yashwanth.bookNstay.service.impl;

import com.yashwanth.bookNstay.Exception.ResourceNotFoundException;
import com.yashwanth.bookNstay.dto.BookingDto;
import com.yashwanth.bookNstay.dto.ProfileUpdateRequestDto;
import com.yashwanth.bookNstay.dto.UserDto;
import com.yashwanth.bookNstay.entity.Booking;
import com.yashwanth.bookNstay.entity.User;
import com.yashwanth.bookNstay.repository.BookingRepository;
import com.yashwanth.bookNstay.repository.UserRepository;
import com.yashwanth.bookNstay.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.yashwanth.bookNstay.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;

    @Override
    public User getUserById(Long id) {
        log.info("finding the user with id: {}", id);
        return userRepository.findById(id).orElseThrow(() ->
                {return new ResourceNotFoundException("User not found with id: "+ id);}
        );
    }

    @Override
    public void updateProfile(ProfileUpdateRequestDto profileDto) {
        User user = getCurrentUser();

        if(profileDto.getDateOfBirth()!=null) user.setDateOfBirth(profileDto.getDateOfBirth());
        if(profileDto.getName()!=null) user.setName(profileDto.getName());
        if(profileDto.getGender()!=null) user.setGender(profileDto.getGender());

        userRepository.save(user);
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();

        List<Booking> bookings = bookingRepository.findByUser(user);
        return bookings.stream()
                .map(booking -> modelMapper.map(booking,BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getMyProfile() {
        User user = getCurrentUser();
        return modelMapper.map(user,UserDto.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(()->new BadCredentialsException("The user with " +username+" not found"));
    }
}
