package com.resumematcher.user;

import com.resumematcher.user.dto.UpdateProfileRequest;
import com.resumematcher.user.dto.UserProfileDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userProfileRepository);
    }

    @Test
    void testGetProfileSuccess() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("jane@example.com");

        UserProfile profile = new UserProfile();
        profile.setId(UUID.randomUUID());
        profile.setUser(user);
        profile.setFullName("Jane Doe");
        profile.setLocation("San Francisco, CA");
        profile.setHeadline("Software Architect");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        UserProfileDto dto = userService.getProfileByEmail("jane@example.com");

        assertNotNull(dto);
        assertEquals("Jane Doe", dto.getFullName());
        assertEquals("San Francisco, CA", dto.getLocation());
        assertEquals("Software Architect", dto.getHeadline());
    }

    @Test
    void testUpdateProfileSuccess() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("jane@example.com");

        UserProfile profile = new UserProfile();
        profile.setId(UUID.randomUUID());
        profile.setUser(user);
        profile.setFullName("Jane Doe");

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Jane Smith");
        request.setLocation("New York, NY");
        request.setHeadline("Lead Engineer");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArgument(0));

        UserProfileDto updated = userService.updateProfile("jane@example.com", request);

        assertNotNull(updated);
        assertEquals("Jane Smith", updated.getFullName());
        assertEquals("New York, NY", updated.getLocation());
        assertEquals("Lead Engineer", updated.getHeadline());
    }
}
