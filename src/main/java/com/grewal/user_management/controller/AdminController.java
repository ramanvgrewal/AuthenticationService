package com.grewal.user_management.controller;

import com.grewal.user_management.dto.RegisterRequestDTO;
import com.grewal.user_management.dto.UserDTO;
import com.grewal.user_management.service.AuthenticationService;
import com.grewal.user_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthenticationService authenticationService;

    private final UserService userService;

    @PostMapping("/register_admin")
    public ResponseEntity<String> registerAdminUser(@RequestBody RegisterRequestDTO registerRequestDTO) {
        return ResponseEntity.ok(authenticationService.registerAdminUser(registerRequestDTO));
    }
    @PostMapping("/register_student")
    public ResponseEntity<String> registerNormalUser(@RequestBody RegisterRequestDTO registerRequestDTO) {
        return ResponseEntity.ok(authenticationService.registerStudent(registerRequestDTO));
    }
    @PostMapping("register_Teacher")
    public ResponseEntity<String> registerTeacherUser(@RequestBody RegisterRequestDTO registerRequestDTO) {
        return ResponseEntity.ok(authenticationService.registerTeacher(registerRequestDTO));
    }
    @DeleteMapping("/delete_student/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
