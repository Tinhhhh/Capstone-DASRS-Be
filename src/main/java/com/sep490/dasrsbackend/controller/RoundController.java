package com.sep490.dasrsbackend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/round")
@RequiredArgsConstructor
@Tag(name = "Round", description = "Round required to use to create tournament.")
public class RoundController {
}
