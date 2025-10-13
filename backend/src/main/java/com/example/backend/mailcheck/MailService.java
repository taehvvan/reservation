package com.example.backend.mailcheck;

import org.springframework.stereotype.Service;

import com.example.backend.register.UserRepository;

@Service
public class MailService {

    private final UserRepository userRepository;

    public MailService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

}
