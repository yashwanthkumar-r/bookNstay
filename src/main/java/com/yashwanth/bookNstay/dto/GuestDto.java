package com.yashwanth.bookNstay.dto;

import com.yashwanth.bookNstay.entity.User;
import com.yashwanth.bookNstay.entity.enums.Gender;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GuestDto {
    private Long id;
    private String name;
    private Integer age;
    private LocalDateTime createdAt;
    private Gender gender;
    private User user;

}
