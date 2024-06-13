package com.technodev.capita.service.implementation;

import com.technodev.capita.domain.Role;
import com.technodev.capita.repository.RoleRepository;
import com.technodev.capita.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository<Role> roleRepository;
    @Override
    public Role getRoleByUserId(Long id) {
        return roleRepository.getRoleByUserId(id);
    }
}
