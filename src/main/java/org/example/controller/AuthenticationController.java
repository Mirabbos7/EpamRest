package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.SignInRequest;
import org.example.dto.response.JwtAuthenticationResponse;
import org.example.security.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

// TODO:
//  1. @RestController includes @Controller
//  2. @RestController includes @ResponseBody, and with Jackson autoconfigured, it will serve JSON
//  3. Why did RESTful approach disappear?
@Controller
@RestController
@RequestMapping(produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signOut")
    public ResponseEntity<String> signOut(
            @RequestHeader("Authorization") String authHeader) {
        log.info("POST /signOut");
        authenticationService.signOut(authHeader);
        log.info("User signed out successfully");
        return ResponseEntity.ok("Signed out successfully");
    }

    // TODO:
    //  Server answer for invalid credentials is 500 and without any readable details.
    /// {"message":"Internal server error","status":500,"timestamp":"2026-03-31T12:11:20.91905"}
    @PostMapping("/signIn")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request){
        return authenticationService.signIn(request);
    }

    @PatchMapping("/{id}/promote")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> promoteToAdmin(@PathVariable Long id){
        authenticationService.promoteToAdmin(id);
        return ResponseEntity.ok("User promoted to admin");
    }
}
