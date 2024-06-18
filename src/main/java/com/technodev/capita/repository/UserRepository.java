package com.technodev.capita.repository;

import com.technodev.capita.domain.User;
import com.technodev.capita.dto.UserDTO;

import java.util.Collection;

public interface UserRepository <T extends User> {

    /* Basic crud Operation */
    T create ( T data );

    Collection<T> list (int page , int pageSize);

    T get(Long id);

    T update(T data);

    Boolean delete(Long id);

    T getUserByEmail(String email);

    void sendVerificationCode(UserDTO userDTO);

    User verifyCode(String email, String code);

    void resetPassword(String email);

    User verifyPasswordKey(String key);

    void renewPassword(String key, String password, String confirmPassword);

    User verifyAccountKey(String key);

    /* more complex operation */






}
