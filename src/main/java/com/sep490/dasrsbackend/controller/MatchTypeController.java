package com.sep490.dasrsbackend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/match-type")
@RequiredArgsConstructor
@Tag(name = "MatchType", description = "Match type required to use for round.")
public class MatchTypeController {
}
