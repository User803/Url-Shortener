package com.project.urlshortener.controllers;

import com.project.urlshortener.ApplicationProperties;
import com.project.urlshortener.dtos.CreateShortUrlForm;
import com.project.urlshortener.exceptions.ShortUrlNotFoundException;
import com.project.urlshortener.model.CreateShortUrlCmd;
import com.project.urlshortener.model.ShortUrl;
import com.project.urlshortener.model.ShortUrlDto;
import com.project.urlshortener.services.ShortUrlService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final ShortUrlService shortUrlService;
    private final ApplicationProperties properties;

    public HomeController(ShortUrlService shortUrlService, ApplicationProperties properties) {
        this.shortUrlService = shortUrlService;
        this.properties = properties;
    }

    @GetMapping("/")
    String home(Model model) {
//        List<ShortUrl> shortUrls = shortUrlRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
//        List<ShortUrl> shortUrls = shortUrlRepository.findByIsPrivateIsFalseOrderByCreatedAtDesc();
        List<ShortUrlDto> shortUrls = shortUrlService.findAllPublicShortUrls();
        model.addAttribute("shortUrls", shortUrls);
        model.addAttribute("baseUrl", properties.baseUrl());
        model.addAttribute("createShortUrlForm", new CreateShortUrlForm(""));
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
            List<ShortUrlDto> shortUrls = shortUrlService.findAllPublicShortUrls();
            model.addAttribute("shortUrls", shortUrls);
            model.addAttribute("baseUrl", properties.baseUrl());
            return "index";
        }

        try {
            CreateShortUrlCmd cmd = new CreateShortUrlCmd(createShortUrlForm.originalUrl());
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
        Optional<ShortUrlDto> shortUrlDtoOptional = shortUrlService.accessShortUrl(shortKey);

        if (shortUrlDtoOptional.isEmpty()) {
            throw new ShortUrlNotFoundException(shortKey);
        }

        ShortUrlDto shortUrlDto = shortUrlDtoOptional.get();
        return "redirect:" + shortUrlDto.originalUrl();
    }
}
