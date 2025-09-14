package com.grewal.user_management.controller;

import com.grewal.user_management.dto.LoginRequestDTO;
import com.grewal.user_management.dto.LoginResponseDTO;
import com.grewal.user_management.dto.RegisterRequestDTO;
import com.grewal.user_management.dto.UserDTO;
import com.grewal.user_management.model.User;
import com.grewal.user_management.repository.UserRepository;
import com.grewal.user_management.service.AuthenticationService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO loginResponseDTO = authenticationService.login(loginRequestDTO);

        ResponseCookie cookie = ResponseCookie.from("JWT", loginResponseDTO.getJwtToken())
                .httpOnly(true).secure(true).path("/").maxAge(60 * 60)
                .sameSite("Strict").build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(loginResponseDTO.getUserdto());
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return authenticationService.logout();
    }
    @GetMapping("/get_current_user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User is not authenticated");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User is not found"));

        return ResponseEntity.ok(convertToUserDTO(user));
    }
    private UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        return userDTO;
    }
}
