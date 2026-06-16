package com.project.urlshortener.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ShortUrlNotFoundException.class)
    String handleShortUrlException(ShortUrlNotFoundException e) {
        log.error("Short URL not found: {}",e.getMessage(), e);
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    String handleException(Exception e) {
        log.error("Short URL not found: {}",e.getMessage(), e);
        return "error/500";
    }

}
