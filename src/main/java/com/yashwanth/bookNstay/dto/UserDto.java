package com.yashwanth.bookNstay.dto;

import com.yashwanth.bookNstay.entity.enums.Role;

import java.util.Set;

public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String password;
    private Set<Role> roles;

}
