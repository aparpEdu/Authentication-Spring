package com.example.googleauthenticationspring.bootstrap;

import com.example.googleauthenticationspring.role.Role;
import com.example.googleauthenticationspring.role.RoleRepository;
import com.example.googleauthenticationspring.user.User;
import com.example.googleauthenticationspring.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@AllArgsConstructor
public class DataCommandLineRunner implements CommandLineRunner {


    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {

        if(roleRepository.count() == 0 && userRepository.count() == 0) {

            Role roleAdmin = new Role();
            Role roleUser = new Role();
            roleUser.setName("ROLE_USER");
            roleAdmin.setName("ROLE_ADMIN");
            roleRepository.save(roleAdmin);
            roleRepository.save(roleUser);


            Set<Role> rolesAdmin = new HashSet<>();
            rolesAdmin.add(roleAdmin);

            Set<Role> rolesUser = new HashSet<>();
            rolesUser.add(roleUser);

            User admin = new User();
            admin.setFirstName("admin");
            admin.setLastName("admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(new BCryptPasswordEncoder().encode("!Admin123"));
            admin.setCreatedAt(LocalDateTime.now());
            admin.setIsVerified(false);
            admin.setRoles(rolesAdmin);
            userRepository.save(admin);

            User martin = new User();
            martin.setFirstName("Martin");
            martin.setLastName("Ivanov");
            martin.setEmail("martin@gmail.com");
            martin.setPassword(new BCryptPasswordEncoder().encode("!Martin123"));
            martin.setCreatedAt(LocalDateTime.now());
            martin.setIsVerified(false);
            martin.setRoles(rolesUser);
            userRepository.save(martin);
        }
    }
}