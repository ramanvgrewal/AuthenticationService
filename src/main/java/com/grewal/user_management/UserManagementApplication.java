package com.grewal.user_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class UserManagementApplication {

    public static void main(String[] args) {

        // uncomment this env for personal use.

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(e -> {
            if (System.getProperty(e.getKey()) == null && System.getenv(e.getKey()) == null) {
                System.setProperty(e.getKey(), e.getValue());
            }
        });

        SpringApplication.run(UserManagementApplication.class, args);

        System.out.println("UserManagementApplication started");
    }

}
