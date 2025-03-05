package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Add custom queries here if needed
}
