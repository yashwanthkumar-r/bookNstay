package com.yashwanth.bookNstay.dto;

import com.yashwanth.bookNstay.entity.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelPriceDto {

   private Hotel hotel;

   private Double price;
}
