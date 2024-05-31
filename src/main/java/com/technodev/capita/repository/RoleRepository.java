package com.technodev.capita.repository;

import com.technodev.capita.domain.Role;

import java.util.Collection;

public interface RoleRepository <T extends Role>{
    /* Basic crud Operation */
    T create ( T data );

    Collection<T> list (int page , int pageSize);

    T get(Long id);

    T update(T data);

    Boolean delete(Long id);

    /* more complex operation */
    void addRoleToUser(Long id , String roleName);
    Role getRoleByUserId(Long userId);
    Role getRoleByUserEmail(String email);
    void updateUserRole(Long userId, String roleName);
}
