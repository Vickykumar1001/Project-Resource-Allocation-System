package com.tcs.auth.mapper;

import com.tcs.auth.dto.UserDto;
import com.tcs.auth.entity.Role;
import com.tcs.auth.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDto toDto(User u) {
        if (u == null) return null;
        return new UserDto(u.getId(), u.getFullName(), u.getEmail(), 
                u.getRole() != null ? u.getRole().name() : null);
    }

    public static User fromRegister(String fullName, String email, String encodedPassword) {
        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPassword(encodedPassword);
        u.setRole(Role.EMPLOYEE);
        u.setDeleted(false);
        return u;
    }

    public static List<UserDto> toDtoList(List<User> users) {
        return users.stream().map(UserMapper::toDto).collect(Collectors.toList());
    }
}
