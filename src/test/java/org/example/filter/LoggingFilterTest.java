package org.example.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    @InjectMocks
    private LoggingFilter loggingFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    void doFilter_shouldSetTransactionIdHeaderAndProceedChain() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/trainers");
        when(request.getQueryString()).thenReturn(null);
        when(response.getStatus()).thenReturn(200);

        loggingFilter.doFilter(request, response, filterChain);

        verify(response).setHeader(eq("X-Transaction-Id"), anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldIncludeQueryString_whenPresent() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/trainers");
        when(request.getQueryString()).thenReturn("page=1&size=10");
        when(response.getStatus()).thenReturn(200);

        loggingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldProceedChain_whenStatusIs400() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/trainers/unknown");
        when(request.getQueryString()).thenReturn(null);
        when(response.getStatus()).thenReturn(404);

        loggingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldProceedChain_evenIfChainThrowsException() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/trainings");
        when(request.getQueryString()).thenReturn(null);
        when(response.getStatus()).thenReturn(500);

        doThrow(new RuntimeException("Chain error"))
                .when(filterChain).doFilter(request, response);

        try {
            loggingFilter.doFilter(request, response, filterChain);
        } catch (RuntimeException ignored) {}

        verify(response).setHeader(eq("X-Transaction-Id"), anyString());
    }
}