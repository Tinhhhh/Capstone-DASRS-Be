package com.sep490.dasrsbackend.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account-cars")
@Tag(name = "AccountCar", description = "Account Car API for unity")
public class AccountCarController {


}
