package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Review;
import com.sep490.dasrsbackend.model.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMatchId(Long matchId);

    List<Review> findByStatusNot(ReviewStatus status);
}
