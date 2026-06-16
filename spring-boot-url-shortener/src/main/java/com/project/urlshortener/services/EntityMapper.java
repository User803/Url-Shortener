package com.project.urlshortener.services;

import com.project.urlshortener.model.ShortUrl;
import com.project.urlshortener.model.ShortUrlDto;
import com.project.urlshortener.model.User;
import com.project.urlshortener.model.UserDto;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public ShortUrlDto toShortUrlDto(ShortUrl shortUrl) {
        UserDto userDto = null;
        if (shortUrl.getCreatedBy() != null) {
            userDto = toUserDto(shortUrl.getCreatedBy());
        }

        return new ShortUrlDto(
                shortUrl.getId(),
                shortUrl.getShortKey(),
                shortUrl.getOriginalUrl(),
                shortUrl.getPrivate(),
                shortUrl.getExpiresAt(),
                userDto,
                shortUrl.getClickCount(),
                shortUrl.getCreatedAt()
        );
    }

    private UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName());
    }
}
