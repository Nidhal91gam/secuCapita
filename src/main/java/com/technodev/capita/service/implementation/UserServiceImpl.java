package com.technodev.capita.service.implementation;


import com.technodev.capita.domain.User;
import com.technodev.capita.dto.UserDTO;
import com.technodev.capita.dtomapper.UserDTOMapper;
import com.technodev.capita.repository.UserRepository;
import com.technodev.capita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository<User> userRepository;

    @Override
    public UserDTO createUser(User user) {
        return UserDTOMapper.fromUser(userRepository.create(user));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        return UserDTOMapper.fromUser(userRepository.getUserByEmail(email));
    }

}
