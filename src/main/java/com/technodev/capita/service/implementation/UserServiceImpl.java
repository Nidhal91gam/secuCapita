package com.technodev.capita.service.implementation;


import com.technodev.capita.domain.Role;
import com.technodev.capita.domain.User;
import com.technodev.capita.dto.UserDTO;
import com.technodev.capita.dtomapper.UserDTOMapper;
import com.technodev.capita.repository.RoleRepository;
import com.technodev.capita.repository.UserRepository;
import com.technodev.capita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository<User> userRepository;
    private final RoleRepository<Role> roleRepository;

    @Override
    public UserDTO createUser(User user) {
        return mapToUserDTO(userRepository.create(user));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        return UserDTOMapper.fromUser(userRepository.getUserByEmail(email));
    }
    @Override
    public void sendVerificationCode(UserDTO userDTO) {
        userRepository.sendVerificationCode(userDTO);
    }
    @Override
    public UserDTO verifyCode(String email, String code) {
        return mapToUserDTO(userRepository.verifyCode(email,code));
    }
    public UserDTO mapToUserDTO(User user) {
        return UserDTOMapper.fromUser(user , roleRepository.getRoleByUserId(user.getId()));
    }



}
