package com.project.urlshortener.controllers;

import com.project.urlshortener.ApplicationProperties;
import com.project.urlshortener.dtos.CreateShortUrlForm;
import com.project.urlshortener.exceptions.ShortUrlNotFoundException;
import com.project.urlshortener.model.*;
import com.project.urlshortener.services.ShortUrlService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final ShortUrlService shortUrlService;
    private final ApplicationProperties properties;
    private final SecurityUtils securityUtils;

    public HomeController(ShortUrlService shortUrlService, ApplicationProperties properties, SecurityUtils securityUtils) {
        this.shortUrlService = shortUrlService;
        this.properties = properties;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/")
    String home(
            @RequestParam(defaultValue = "1") Integer page,
            Model model) {
//        User currentUser = securityUtils.getCurrentUser();
//        List<ShortUrl> shortUrls = shortUrlRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
//        List<ShortUrl> shortUrls = shortUrlRepository.findByIsPrivateIsFalseOrderByCreatedAtDesc();
        PagedResult<ShortUrlDto> shortUrls = shortUrlService.findAllPublicShortUrls(page, properties.pageSize());
        model.addAttribute("shortUrls", shortUrls);
        model.addAttribute("baseUrl", properties.baseUrl());
        model.addAttribute("createShortUrlForm", new CreateShortUrlForm("", false, null));
        return "index";
    }

    @GetMapping("/login")
    String loginForm() {
        return "login";
    }

    @PostMapping("/short-urls")
    String createShortUrl(@ModelAttribute("createShortUrlForm") @Valid CreateShortUrlForm createShortUrlForm,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            PagedResult<ShortUrlDto> shortUrls = shortUrlService.findAllPublicShortUrls(1, properties.pageSize());
            model.addAttribute("shortUrls", shortUrls);
            model.addAttribute("baseUrl", properties.baseUrl());
            return "index";
        }

        try {
            Long userId = securityUtils.getCurrentUserId();
            CreateShortUrlCmd cmd = new CreateShortUrlCmd(
                    createShortUrlForm.originalUrl(),
                    createShortUrlForm.isPrivate(),
                    createShortUrlForm.expirationInDays(),
                    userId
            );
            ShortUrlDto shortUrlDto = shortUrlService.createShortUrl(cmd);

            // Using RedirectAttributes to follow the Post/Redirect/Get pattern to avoid duplicate requests i.e., when form data is submitted redirect elsewhere to prevent duplicate POST requests
            redirectAttributes.addFlashAttribute("successMessage", "Short Url created successfully - " +
                    properties.baseUrl() + "/s/" + shortUrlDto.shortKey());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create Short Url");
        }

        return "redirect:/";
    }

    @GetMapping("/s/{shortKey}")
    String redirectToOriginalUrl(@PathVariable String shortKey, Model model) {
        Long userId = securityUtils.getCurrentUserId();
        Optional<ShortUrlDto> shortUrlDtoOptional = shortUrlService.accessShortUrl(shortKey, userId);
        if (shortUrlDtoOptional.isEmpty()) {
            throw new ShortUrlNotFoundException(shortKey);
        }

        ShortUrlDto shortUrlDto = shortUrlDtoOptional.get();
        return "redirect:" + shortUrlDto.originalUrl();
    }
}
