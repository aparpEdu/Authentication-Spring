package com.example.googleauthenticationspring.authentication;

import com.example.googleauthenticationspring.role.Role;
import com.example.googleauthenticationspring.role.RoleRepository;
import com.example.googleauthenticationspring.user.User;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class AuthenticationHelper {

    private final RoleRepository roleRepository;

    public AuthenticationHelper(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public User setRoles(User user) {
        Set<Role> roles = new HashSet<>();
        Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
        Role role = new Role();
        if (userRole.isPresent()) {
            role = userRole.get();
        }
        roles.add(role);
        user.setRoles(roles);
        return user;
    }
}
