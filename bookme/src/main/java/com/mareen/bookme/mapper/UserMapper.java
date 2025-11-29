package com.mareen.bookme.mapper;

import com.mareen.bookme.dto.response.UserResponse;
import com.mareen.bookme.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.isEnabled()
        );
    }
}
