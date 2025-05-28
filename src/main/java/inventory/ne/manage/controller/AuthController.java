package inventory.ne.manage.controller;

import inventory.ne.manage.dto.request.LoginRequest;
import inventory.ne.manage.dto.request.RegisterRequest;
import inventory.ne.manage.dto.response.ApiResponse;
import inventory.ne.manage.dto.response.AuthResponse;
import inventory.ne.manage.exception.EmailNotVerifiedException;
import inventory.ne.manage.exception.InvalidCredentialsException;
import inventory.ne.manage.model.User;
import inventory.ne.manage.security.JwtTokenUtil;
import inventory.ne.manage.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthController(AuthenticationManager authenticationManager, UserService userService,
            JwtTokenUtil jwtTokenUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest registerRequest) {
        userService.registerUser(registerRequest);
        return ResponseEntity
                .ok(new ApiResponse(true, "User registered successfully. Please verify your email.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.findByEmail(loginRequest.getEmail());
            if (!user.isEmailVerified()) {
                throw new EmailNotVerifiedException("Please verify your email before logging in.");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenUtil.generateAccessToken(user);
            return ResponseEntity.ok(new AuthResponse(token, "Bearer"));
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam String email) {
        userService.sendForgotPasswordCode(email);
        return ResponseEntity.ok(new ApiResponse(true, "Reset code sent to your email.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestParam String email,
            @RequestParam String code,
            @RequestParam String newPassword) {
        userService.resetPassword(email, code, newPassword);
        return ResponseEntity.ok(new ApiResponse(true, "Password reset successful.", null));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam("email") String email,
            @RequestParam("code") String code) {
        userService.verifyEmail(email, code);
        return ResponseEntity.ok(new ApiResponse(true, "Email verified successfully", null));
    }
}
