package com.grewal.user_management.service;

import com.grewal.user_management.dto.LoginRequestDTO;
import com.grewal.user_management.dto.LoginResponseDTO;
import com.grewal.user_management.dto.RegisterRequestDTO;
import com.grewal.user_management.dto.UserDTO;
import com.grewal.user_management.jwt.JwtService;
import com.grewal.user_management.model.Role;
import com.grewal.user_management.model.StudentProfile;
import com.grewal.user_management.model.TeacherProfile;
import com.grewal.user_management.model.User;
import com.grewal.user_management.repository.StudentProfileRepository;
import com.grewal.user_management.repository.TeacherProfileRepository;
import com.grewal.user_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;

    public String registerStudent(RegisterRequestDTO registerRequestDTO) {
        validateUniqueUserFields(registerRequestDTO);

        User user = new User();
        user.setUsername(registerRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setEmail(registerRequestDTO.getEmail());
        user.setPhone(registerRequestDTO.getPhone());
        user.setActive(true);
        user.setRole(Role.STUDENT);
        user = userRepository.save(user);

        if (teacherProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("User already has a teacher profile");
        }
        StudentProfile profile = new StudentProfile();
        profile.setUser(user);
        studentProfileRepository.save(profile);

        return "Student Registered Successfully";
    }

    private void validateUniqueUserFields(RegisterRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        if (userRepository.existsByPhone(dto.getPhone())) {
            throw new RuntimeException("Phone is already in use");
        }
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        return userDTO;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByUsername(loginRequestDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("Username not found"));

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken
                (loginRequestDTO.getUsername(), loginRequestDTO.getPassword()));

        String jwtToken = jwtService.generateToken(user);

        return LoginResponseDTO.builder().jwtToken(jwtToken)
                .userdto(convertToUserDTO(user)).build();
    }

    public ResponseEntity<String> logout() {
        ResponseCookie cookie = ResponseCookie.from("JWT", "")
                .httpOnly(true).secure(true).path("/")
                .maxAge(0).sameSite("strict").build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logged out");
    }

    public String registerAdminUser(RegisterRequestDTO registerRequestDTO) {
        validateUniqueUserFields(registerRequestDTO);

        User user = new User();
        user.setUsername(registerRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setEmail(registerRequestDTO.getEmail());
        user.setPhone(registerRequestDTO.getPhone());
        user.setActive(true);
        user.setRole(Role.ADMIN);

        userRepository.save(user);
        return "Admin User Registered Successfully";
    }

    public String registerTeacher(RegisterRequestDTO registerRequestDTO) {
        validateUniqueUserFields(registerRequestDTO);

        User user = new User();
        user.setUsername(registerRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setEmail(registerRequestDTO.getEmail());
        user.setPhone(registerRequestDTO.getPhone());
        user.setActive(true);
        user.setRole(Role.TEACHER);
        user = userRepository.save(user);

        if (studentProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("User already has a student profile");
        }
        TeacherProfile profile = new TeacherProfile();
        profile.setUser(user);
        teacherProfileRepository.save(profile);

        return "Teacher Registered Successfully";
    }
}
