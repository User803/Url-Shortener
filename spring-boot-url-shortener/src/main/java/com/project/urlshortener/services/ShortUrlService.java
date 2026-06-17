package com.project.urlshortener.services;

import com.project.urlshortener.ApplicationProperties;
import com.project.urlshortener.model.CreateShortUrlCmd;
import com.project.urlshortener.model.ShortUrl;
import com.project.urlshortener.model.ShortUrlDto;
import com.project.urlshortener.repositories.ShortUrlRepository;
import com.project.urlshortener.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class, readOnly = true)
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final EntityMapper entityMapper;
    private final ApplicationProperties properties;
    private final UserRepository userRepository;

    public ShortUrlService(ShortUrlRepository shortUrlRepository, EntityMapper entityMapper, ApplicationProperties properties, UserRepository userRepository) {
        this.shortUrlRepository = shortUrlRepository;
        this.entityMapper = entityMapper;
        this.properties = properties;
        this.userRepository = userRepository;
    }

    public List<ShortUrlDto> findAllPublicShortUrls() {
        return shortUrlRepository.findPublicShortUrlsEntityGraph()
                .stream()
                .map(entityMapper::toShortUrlDto)
                .toList();
    }

    @Transactional
    public ShortUrlDto createShortUrl(CreateShortUrlCmd shortUrlCmd) {
        // User must be online for this check to work
//        if (properties.validateOriginalUrl()) {
//            boolean urlExists = UrlExistenceValidator.isUrlExists(shortUrlCmd.originalUrl());
//            if (!urlExists) {
//                throw new RuntimeException("Invalid URL " + shortUrlCmd.originalUrl());
//            }
//        }

        var shortKey = generateUniqueShortKey();
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(shortUrlCmd.originalUrl());
        shortUrl.setShortKey(shortKey);
        if (shortUrlCmd.userId() == null) {
            shortUrl.setCreatedBy(null);
            shortUrl.setPrivate(false);
            shortUrl.setExpiresAt(Instant.now().plus(properties.defaultExpiryInDays(), ChronoUnit.DAYS));
        } else {
            shortUrl.setCreatedBy(userRepository.findById(shortUrlCmd.userId()).orElseThrow());
            shortUrl.setPrivate(shortUrlCmd.isPrivate() != null && shortUrlCmd.isPrivate());
            shortUrl.setExpiresAt(shortUrlCmd.expirationInDays() != null ? Instant.now().plus(shortUrlCmd.expirationInDays() , ChronoUnit.DAYS) : null);
        }
        shortUrl.setClickCount(0L);
        shortUrl.setCreatedAt(Instant.now());
        shortUrlRepository.save(shortUrl);

        return entityMapper.toShortUrlDto(shortUrl);
    }

    private String generateUniqueShortKey() {
        String shortKey;
        do {
            shortKey = generateRandomShortKey();
        } while (shortUrlRepository.existsByShortKey(shortKey));

        return shortKey;
    }


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_KEY_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String generateRandomShortKey() {
        StringBuilder sb = new StringBuilder(SHORT_KEY_LENGTH);
        for (int i = 0; i < SHORT_KEY_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        return sb.toString();
    }

    @Transactional
    public Optional<ShortUrlDto> accessShortUrl(String shortKey, Long userId) {
        Optional<ShortUrl> shortUrlOptional = shortUrlRepository.findByShortKey(shortKey);
        if (shortUrlOptional.isEmpty()) {
            return Optional.empty();
        }

        ShortUrl shortUrl = shortUrlOptional.get();
        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }

        // Check if the URL is private if so; this user is the one who added this private URL and is logged in
        if (shortUrl.getPrivate() != null &&
                shortUrl.getCreatedBy() != null &&
                !Objects.equals(shortUrl.getCreatedBy().getId(), userId)) {
            return Optional.empty();
        }

        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);
        return shortUrlOptional.map(entityMapper::toShortUrlDto);
    }
}
