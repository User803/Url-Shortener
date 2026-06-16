package com.project.urlshortener.model;

import java.io.Serializable;

public record UserDto(Long id, String name) implements Serializable {
}
