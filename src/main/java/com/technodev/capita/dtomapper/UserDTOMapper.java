package com.technodev.capita.dtomapper;

import com.technodev.capita.domain.Role;
import com.technodev.capita.domain.User;
import com.technodev.capita.dto.UserDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class UserDTOMapper {
    public static UserDTO fromUser(User user)
    {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user , userDTO);
        return  userDTO;
    }
    public static UserDTO fromUser(User user, Role role)
    {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user , userDTO);
        userDTO.setRoleName(role.getName());
        userDTO.setPermissions(role.getPermission());
        return  userDTO;
    }
    public static User toUser(UserDTO userDTO)
    {
        User user = new User();
        BeanUtils.copyProperties(userDTO , user);
        return  user;
    }
}
