package com.project.urlshortener.controllers;

import com.project.urlshortener.dtos.CreateUserCmd;
import com.project.urlshortener.dtos.RegisterUserRequest;
import com.project.urlshortener.model.Role;
import com.project.urlshortener.services.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new RegisterUserRequest("", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") RegisterUserRequest registerUserRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            var cmd = new CreateUserCmd(
                    registerUserRequest.email(),
                    registerUserRequest.password(),
                    registerUserRequest.name(),
                    Role.ROLE_USER
            );

            userService.createUser(cmd);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully. Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }
}
