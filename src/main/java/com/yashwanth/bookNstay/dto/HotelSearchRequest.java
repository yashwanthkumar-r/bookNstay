package com.yashwanth.bookNstay.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HotelSearchRequest {

    private String city;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer roomsCount;

    //To make response paginated we ask for Page_num and size of page
    private Integer page = 0;
    private Integer size = 10;
}
