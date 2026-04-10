package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.service.AuthenticationService;
import org.example.config.service.JwtTokenService;
import org.example.dto.request.SignInRequest;
import org.example.dto.response.JwtAuthenticationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void signIn_shouldReturn200_withToken() throws Exception {
        SignInRequest request = new SignInRequest("john.doe", "pass123");
        JwtAuthenticationResponse response = new JwtAuthenticationResponse("jwt.token");

        when(authenticationService.signIn(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("jwt.token")));

        verify(authenticationService).signIn(any());
    }

    @Test
    void signIn_shouldReturn400_whenBodyInvalid() throws Exception {
        SignInRequest request = new SignInRequest(null, null);

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
    }

    @Test
    void signOut_shouldReturn200_withMessage() throws Exception {
        doNothing().when(authenticationService).signOut(any());

        mockMvc.perform(post("/api/auth/sign-out")
                        .header("Authorization", "Bearer jwt.token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Signed out successfully"));

        verify(authenticationService).signOut(eq("Bearer jwt.token"));
    }

    @Test
    void signOut_shouldReturn400_whenAuthHeaderMissing() throws Exception {
        mockMvc.perform(post("/api/auth/sign-out"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
    }

    @Test
    void promoteToAdmin_shouldReturn200() throws Exception {
        doNothing().when(authenticationService).promoteToAdmin(1L);

        mockMvc.perform(patch("/api/auth/1/role"))
                .andExpect(status().isOk())
                .andExpect(content().string("User promoted to admin"));

        verify(authenticationService).promoteToAdmin(1L);
    }

    @Test
    void promoteToAdmin_shouldReturn400_whenIdIsNotNumber() throws Exception {
        mockMvc.perform(patch("/api/auth/abc/role"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
    }
}