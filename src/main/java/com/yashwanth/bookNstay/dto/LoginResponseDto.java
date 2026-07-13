package com.yashwanth.bookNstay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String accessToken;
}
