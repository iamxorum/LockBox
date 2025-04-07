package com.lockbox.config;

import com.lockbox.model.*;
import com.lockbox.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@Profile("dev")
public class DevDataInitializer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initDatabase(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            PasswordRepository passwordRepository,
            SecureNoteRepository secureNoteRepository,
            AuditLogRepository auditLogRepository,
            LoginAttemptRepository loginAttemptRepository) {
        
        return args -> {
            if (userRepository.count() > 0) {
                System.out.println("Database already contains data, skipping initialization");
                return;
            }
            
            System.out.println("Initializing database with mock data...");
            
            // Create users
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@lockbox.com");
            admin.setPassword(passwordEncoder().encode("admin"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setCreatedAt(LocalDateTime.now());
            
            User john = new User();
            john.setUsername("johndoe");
            john.setEmail("john@example.com");
            john.setPassword(passwordEncoder().encode("password123"));
            john.setFirstName("John");
            john.setLastName("Doe");
            john.setCreatedAt(LocalDateTime.now());
            
            User alice = new User();
            alice.setUsername("aliceb");
            alice.setEmail("alice@example.com");
            alice.setPassword(passwordEncoder().encode("password123"));
            alice.setFirstName("Alice");
            alice.setLastName("Brown");
            alice.setCreatedAt(LocalDateTime.now());
            
            userRepository.saveAll(Arrays.asList(admin, john, alice));
            
            // Create categories
            Category work = new Category();
            work.setName("Work");
            work.setDescription("Work-related accounts");
            work.setUser(john);
            work.setCreatedAt(LocalDateTime.now());
            
            Category personal = new Category();
            personal.setName("Personal");
            personal.setDescription("Personal accounts");
            personal.setUser(john);
            personal.setCreatedAt(LocalDateTime.now());
            
            Category social = new Category();
            social.setName("Social Media");
            social.setDescription("Social media accounts");
            social.setUser(john);
            social.setCreatedAt(LocalDateTime.now());
            
            Category financial = new Category();
            financial.setName("Financial");
            financial.setDescription("Banking and financial accounts");
            financial.setUser(alice);
            financial.setCreatedAt(LocalDateTime.now());
            
            categoryRepository.saveAll(Arrays.asList(work, personal, social, financial));
            
            // Create tags
            Tag important = new Tag();
            important.setName("Important");
            important.setCreatedAt(LocalDateTime.now());
            
            Tag favorite = new Tag();
            favorite.setName("Favorite");
            favorite.setCreatedAt(LocalDateTime.now());
            
            Tag sensitive = new Tag();
            sensitive.setName("Sensitive");
            sensitive.setCreatedAt(LocalDateTime.now());
            
            Tag archived = new Tag();
            archived.setName("Archived");
            archived.setCreatedAt(LocalDateTime.now());
            
            tagRepository.saveAll(Arrays.asList(important, favorite, sensitive, archived));
            
            // Create passwords
            Password gmail = new Password();
            gmail.setTitle("Gmail");
            gmail.setUsername("john.doe@gmail.com");
            gmail.setPasswordValue("Gmailp@ss123");
            gmail.setUrl("https://mail.google.com");
            gmail.setNotes("My main email account");
            gmail.setUser(john);
            gmail.setCategory(personal);
            gmail.setCreatedAt(LocalDateTime.now());
            gmail.getTags().add(important);
            gmail.getTags().add(favorite);
            
            Password github = new Password();
            github.setTitle("GitHub");
            github.setUsername("john_dev");
            github.setPasswordValue("G1thubP@ss!");
            github.setUrl("https://github.com");
            github.setNotes("Work GitHub account");
            github.setUser(john);
            github.setCategory(work);
            github.setCreatedAt(LocalDateTime.now());
            github.getTags().add(important);
            
            Password netflix = new Password();
            netflix.setTitle("Netflix");
            netflix.setUsername("johndoe@example.com");
            netflix.setPasswordValue("N3tflixP@ss!");
            netflix.setUrl("https://netflix.com");
            netflix.setNotes("Family Netflix account");
            netflix.setUser(john);
            netflix.setCategory(personal);
            netflix.setCreatedAt(LocalDateTime.now());
            netflix.getTags().add(favorite);
            
            Password banking = new Password();
            banking.setTitle("Online Banking");
            banking.setUsername("aliceb");
            banking.setPasswordValue("S3cur3B@nk!");
            banking.setUrl("https://mybank.com");
            banking.setNotes("My primary bank account");
            banking.setUser(alice);
            banking.setCategory(financial);
            banking.setCreatedAt(LocalDateTime.now());
            banking.getTags().add(important);
            banking.getTags().add(sensitive);
            
            passwordRepository.saveAll(Arrays.asList(gmail, github, netflix, banking));
            
            // Create secure notes
            SecureNote meetingNotes = new SecureNote();
            meetingNotes.setTitle("Team Meeting Notes");
            meetingNotes.setContent("1. Discussed project timeline\n2. Assigned tasks\n3. Next meeting on Friday");
            meetingNotes.setUser(john);
            meetingNotes.setCategory(work);
            meetingNotes.setCreatedAt(LocalDateTime.now());
            meetingNotes.getTags().add(important);
            
            SecureNote wifiSettings = new SecureNote();
            wifiSettings.setTitle("Home WiFi Setup");
            wifiSettings.setContent("SSID: HomeNetwork\nPassword: Wif1P@ssw0rd\nSecurity: WPA2");
            wifiSettings.setUser(john);
            wifiSettings.setCategory(personal);
            wifiSettings.setCreatedAt(LocalDateTime.now());
            wifiSettings.getTags().add(important);
            
            SecureNote travelPlans = new SecureNote();
            travelPlans.setTitle("Summer Vacation Plans");
            travelPlans.setContent("Flight: AA123\nDeparture: June 15\nHotel: Beach Resort\nReservation #: 12345");
            travelPlans.setUser(alice);
            travelPlans.setCreatedAt(LocalDateTime.now());
            
            secureNoteRepository.saveAll(Arrays.asList(meetingNotes, wifiSettings, travelPlans));
            
            // Create audit logs
            AuditLog log1 = new AuditLog();
            log1.setUser(john);
            log1.setAction("CREATE");
            log1.setEntityType("PASSWORD");
            log1.setEntityId(gmail.getId());
            log1.setDetails("Created password entry for Gmail");
            log1.setTimestamp(LocalDateTime.now().minusDays(2));
            
            AuditLog log2 = new AuditLog();
            log2.setUser(john);
            log2.setAction("UPDATE");
            log2.setEntityType("PASSWORD");
            log2.setEntityId(github.getId());
            log2.setDetails("Updated password for GitHub");
            log2.setTimestamp(LocalDateTime.now().minusDays(1));
            
            AuditLog log3 = new AuditLog();
            log3.setUser(alice);
            log3.setAction("CREATE");
            log3.setEntityType("SECURE_NOTE");
            log3.setEntityId(travelPlans.getId());
            log3.setDetails("Created new secure note for travel plans");
            log3.setTimestamp(LocalDateTime.now().minusHours(12));
            
            auditLogRepository.saveAll(Arrays.asList(log1, log2, log3));
            
            // Create login attempts
            LoginAttempt login1 = new LoginAttempt();
            login1.setUsername(john.getUsername());
            login1.setSuccessful(true);
            login1.setIpAddress("192.168.1.1");
            login1.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            login1.setTimestamp(LocalDateTime.now().minusDays(2));
            
            LoginAttempt login2 = new LoginAttempt();
            login2.setUsername(john.getUsername());
            login2.setSuccessful(false);
            login2.setIpAddress("192.168.1.100");
            login2.setUserAgent("Mozilla/5.0 (Linux; Android 10)");
            login2.setTimestamp(LocalDateTime.now().minusDays(1));
            
            LoginAttempt login3 = new LoginAttempt();
            login3.setUsername(alice.getUsername());
            login3.setSuccessful(true);
            login3.setIpAddress("192.168.1.50");
            login3.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)");
            login3.setTimestamp(LocalDateTime.now().minusHours(6));
            
            loginAttemptRepository.saveAll(Arrays.asList(login1, login2, login3));
            
            System.out.println("Database initialization completed!");
        };
    }
} 