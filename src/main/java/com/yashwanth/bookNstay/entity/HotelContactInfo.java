package com.yashwanth.bookNstay.entity;


import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Setter
@Getter
public class HotelContactInfo {

    private String address;

    private String location;

    private String email;

    private String phoneNumber;

}
