package com.technodev.capita.filter;

import com.technodev.capita.provider.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.technodev.capita.utils.ExceptionUtils.processError;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    private static final String TOKEN_PRIFIX = "Bearer ";
    private static final String[] PUBLIC_ROUTES = {"/user/login","/user/verify/code","/user/register","/user/refresh/token"} ;
    private static final String HTTP_OPTIONS_METHOD = "OPTIONS";
    private final TokenProvider tokenProvider;
    protected static final String TOKEN_KEY = "token";
    protected static final String EMAIL_KEY = "email";
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            Map<String ,String> values = getRequestValues(request);

            String token = getToken(request);
            if(tokenProvider.isTokenValid(values.get(EMAIL_KEY), token)){

                List<GrantedAuthority> authorities=tokenProvider.getAuthorities(values.get(TOKEN_KEY));

                Authentication authentication = tokenProvider.getAuthentication(values.get(EMAIL_KEY), authorities, request);

                SecurityContextHolder.getContext().setAuthentication(authentication);

            }else
            {SecurityContextHolder.clearContext();}
            filterChain.doFilter(request,response);
        }catch (Exception  exception){
            log.error(exception.getMessage());
            processError(request, response, exception);
        }
    }

    private String getToken(HttpServletRequest request) {
        return ofNullable(request.getHeader(AUTHORIZATION))
                .filter(header->header.startsWith(TOKEN_PRIFIX))
                .map(token-> token.replace(TOKEN_PRIFIX, EMPTY)).get();
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getHeader(AUTHORIZATION) == null
                || !request.getHeader(AUTHORIZATION).startsWith(TOKEN_PRIFIX)
                || request.getMethod().equalsIgnoreCase(HTTP_OPTIONS_METHOD)
                || asList(PUBLIC_ROUTES).contains(request.getRequestURI());
    }
    Map<String ,String> getRequestValues(HttpServletRequest request){
        return Map.of(EMAIL_KEY, tokenProvider.getSubject(getToken(request), request),TOKEN_KEY, getToken(request));
    }
}
