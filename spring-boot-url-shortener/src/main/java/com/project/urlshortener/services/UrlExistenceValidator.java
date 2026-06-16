package com.project.urlshortener.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class UrlExistenceValidator {
    private static final Logger LOG = LoggerFactory.getLogger(UrlExistenceValidator.class);

    public static boolean isUrlExists(String urlString) {
        try {
            LOG.debug("Checking if url exists: {}", urlString);
            URL url = new URI(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 & responseCode < 400);
        } catch (Exception e) {
            LOG.error("Error while checking URL: {}", urlString, e);
            return false;
        }
    }
}
