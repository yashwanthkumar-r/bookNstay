package com.yashwanth.bookNstay.dto;

import com.yashwanth.bookNstay.entity.Hotel;
import com.yashwanth.bookNstay.entity.Room;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryDto {
    private Long id;
    private Integer bookedCount;
    private Integer totalCount;
    private BigDecimal surgeFactor;
    private BigDecimal price; //basePrice * surgeFactor
    private String city;
    private Boolean closed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime date;
    private Hotel hotel;
    private Room room;

}
