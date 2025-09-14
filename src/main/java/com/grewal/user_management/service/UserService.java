package com.grewal.user_management.service;

import com.grewal.user_management.dto.ChangePasswordDTO;
import com.grewal.user_management.dto.UserDTO;
import com.grewal.user_management.model.User;
import com.grewal.user_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        return userDTO;
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new
                RuntimeException("User not found"));

        return convertToUserDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new RuntimeException("User not found"));
        return convertToUserDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(this::convertToUserDTO).collect(Collectors.toList());
    }

    public UserDTO changePassword(Long id, ChangePasswordDTO changePasswordDTO) {
        User user =  userRepository.findById(id).orElseThrow(() -> new
               RuntimeException("User not found"));

        // matches(raw, encoded)
        if(!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("current password is incorrect");
        }

        if(!changePasswordDTO.getNewPassword()
                .equals(changePasswordDTO.getConfirmPassword())) {
            throw new RuntimeException("new password doesn't match confirm password");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        User savedUser = userRepository.save(user);
        return convertToUserDTO(savedUser);
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user =  userRepository.findById(id).orElseThrow(() -> new
               RuntimeException("User not found"));

        if (!user.getUsername().equals(userDTO.getUsername()) &&
                userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        if (!user.getEmail().equals(userDTO.getEmail()) &&
                userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        if (!user.getPhone().equals(userDTO.getPhone()) &&
                userRepository.existsByPhone(userDTO.getPhone())) {
            throw new RuntimeException("Phone is already in use");
        }

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());

        return convertToUserDTO(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new
               RuntimeException("User not found"));
        userRepository.delete(user);
    }
}
