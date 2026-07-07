package com.charging;

import com.charging.model.User;
import com.charging.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepo, BCryptPasswordEncoder encoder) {
        return args -> {
            if (!userRepo.existsByUsername("admin")) {
                User admin = new User("admin", encoder.encode("admin123"), "admin", "");
                userRepo.save(admin);
                System.out.println("✅ 管理员账号已创建: admin / admin123");
            }
            System.out.println("========================================");
            System.out.println("  充电站管理系统已启动");
            System.out.println("  前端: http://localhost:8080");
            System.out.println("  管理员: admin / admin123");
            System.out.println("========================================");
        };
    }
}
