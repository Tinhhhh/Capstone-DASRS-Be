package com.sep490.dasrsbackend.converter;

import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.entity.Review;
import com.sep490.dasrsbackend.model.payload.request.ReviewRequest;
import com.sep490.dasrsbackend.model.payload.response.ReviewResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReviewConverter {

    @Autowired
    private ModelMapper modelMapper;

    public Review toEntity(com.sep490.dasrsbackend.model.payload.request.ReviewRequest request, Match match) {
        Review review = modelMapper.map(request, Review.class);
        review.setMatch(match);
        return review;
    }

    public com.sep490.dasrsbackend.model.payload.response.ReviewResponse toResponse(Review review) {
        ReviewResponse response = modelMapper.map(review, ReviewResponse.class);
        if (review.getMatch() != null) {
            response.setMatchName(review.getMatch().getMatchName());
        }
        return response;
    }
}
