package com.lockbox.domain.service;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for checking if passwords have been found in known data breaches
 * using the Pwned Passwords API
 */
@Service
public class BreachDetectionService {
    
    private static final Logger logger = Logger.getLogger(BreachDetectionService.class.getName());
    private final RestTemplate restTemplate;
    private final Environment environment;
    
    private static final String PWNED_PASSWORDS_API = "https://api.pwnedpasswords.com/range/";
    
    public BreachDetectionService(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }
    
    /**
     * Checks if a password has been found in known data breaches using the k-anonymity model
     * This uses the freely available Pwned Passwords API that doesn't require an API key
     * 
     * @param password The password to check
     * @return The number of times the password has been exposed in breaches (0 if not found)
     */
    public int checkPasswordPwned(String password) {
        if (password == null || password.isBlank()) {
            return 0;
        }
        
        try {
            // Generate SHA-1 hash of the password
            String sha1Hash = sha1(password).toUpperCase();
            
            // Use k-anonymity model: first 5 chars sent to API, rest used for local check
            String prefix = sha1Hash.substring(0, 5);
            String suffix = sha1Hash.substring(5);
            
            // Call the API with just the prefix
            String url = PWNED_PASSWORDS_API + prefix;
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Parse the response to find if our hash suffix is in the list
                String[] lines = response.getBody().split("\\r?\\n");
                
                for (String line : lines) {
                    String[] parts = line.split(":");
                    if (parts.length == 2 && parts[0].equalsIgnoreCase(suffix)) {
                        // Found a match - return the count
                        return Integer.parseInt(parts[1].trim());
                    }
                }
            }
            
            // Not found in breach database
            return 0;
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error checking password against Pwned Passwords API", e);
            
            // Return 0 on error as we can't confirm it's pwned
            return 0;
        }
    }
    
    /**
     * Converts a string to SHA-1 hash
     */
    private String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = md.digest(input.getBytes());
        
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString();
    }
} 