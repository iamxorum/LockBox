package com.lockbox.client;

import com.lockbox.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserDto getUser(Long id) {
        log.warn("Fallback method called for getUser with id: {}", id);
        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(id);
        fallbackUser.setUsername("fallback-user");
        fallbackUser.setEmail("fallback@example.com");
        return fallbackUser;
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.warn("Fallback method called for getAllUsers");
        return List.of(getUser(0L));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        log.warn("Fallback method called for createUser");
        return getUser(0L);
    }
} 