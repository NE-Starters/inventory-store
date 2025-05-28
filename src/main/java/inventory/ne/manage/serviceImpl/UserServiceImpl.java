package inventory.ne.manage.serviceImpl;

import inventory.ne.manage.dto.request.RegisterRequest;
import inventory.ne.manage.enums.ERole;
import inventory.ne.manage.exception.DuplicateEmailException; 
import inventory.ne.manage.exception.ResourceNotFoundException;
import inventory.ne.manage.model.User;
import inventory.ne.manage.repository.UserRepository;
import inventory.ne.manage.service.EmailService;
import inventory.ne.manage.service.UserService;
import inventory.ne.manage.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerUtil.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    private String generate6DigitCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @Override
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            logger.warn("Registration failed: Email {} already in use", registerRequest.getEmail());
            throw new DuplicateEmailException("Email already in use");
        }
        User user = new User();
        user.setFullname(registerRequest.getFullname());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        Set<ERole> roles = registerRequest.getRoles() != null ? registerRequest.getRoles()
                : Collections.singleton(ERole.ROLE_USER);
        user.setRoles(roles);
        user.setEmailVerified(false);

        String verificationCode = generate6DigitCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationCode);

        logger.info("User registered: {}", savedUser.getEmail());
        return savedUser;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .filter(u -> code.equals(u.getVerificationCode())
                        && u.getVerificationCodeExpiry().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired verification code"));

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);
        logger.info("Email verified for user: {}", user.getEmail());
    }

    @Override
    public void sendForgotPasswordCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        String code = generate6DigitCode();
        user.setResetPasswordCode(code);
        user.setResetPasswordCodeExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        emailService.sendResetPasswordEmail(email, code);
        logger.info("Sent reset password code to: {}", email);
    }

    @Override
    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email)
                .filter(u -> code.equals(u.getResetPasswordCode())
                        && u.getResetPasswordCodeExpiry().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset code"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordCode(null);
        user.setResetPasswordCodeExpiry(null);
        userRepository.save(user);
        logger.info("Password reset for user: {}", email);
    }
}