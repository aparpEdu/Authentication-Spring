package com.example.googleauthenticationspring;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Google Authentication Spring",
        description = "Google Authentication Spring Documentation",
        version = "v1.0",
        contact = @Contact(
                name = "Alexander Parpulansky",
                email = "alexanderparpulansky@gmail.com"
        )
),
        externalDocs = @ExternalDocumentation(
                description = "Google Authentication Spring Documentation",
                url = "https://gitlab.mentormate.bg/base/mmu/foundation/yellow-squad-project/back-end"
        )
)
public class GoogleAuthenticationSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoogleAuthenticationSpringApplication.class, args);
    }

}
