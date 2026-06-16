package com.project.urlshortener.repositories;

import com.project.urlshortener.model.ShortUrl;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
//    List<ShortUrl> findByIsPrivateIsFalseOrderByCreatedAtDesc();

    // Perform left join on createdBy is Lazy loaded and prevent N + 1 Select problem
    @Query("select su from ShortUrl su left join fetch su.createdBy where su.isPrivate = false order by su.createdAt desc")
    List<ShortUrl> findPublicShortUrls();

    // Using is @EntityGraph for createdBy field, which is Lazy loaded, also prevents N + 1 Select problem
    @Query("select su from ShortUrl su where su.isPrivate = false order by su.createdAt desc")
    @EntityGraph(attributePaths = {"createdBy"})
    List<ShortUrl> findPublicShortUrlsEntityGraph();

    boolean existsByShortKey(String shortKey);

    Optional<ShortUrl> findByShortKey(String shortKey);
}

