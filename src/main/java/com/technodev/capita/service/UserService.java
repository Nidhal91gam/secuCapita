package com.technodev.capita.service;

import com.technodev.capita.domain.User;
import com.technodev.capita.dto.UserDTO;

public interface UserService {

    UserDTO createUser(User user);

    UserDTO getUserByEmail(String email);

    void sendVerificationCode(UserDTO userDTO);

    UserDTO verifyCode(String email, String code);
}
