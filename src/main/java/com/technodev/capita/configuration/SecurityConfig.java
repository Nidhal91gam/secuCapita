package com.technodev.capita.configuration;


import com.technodev.capita.filter.CustomAuthorizationFilter;
import com.technodev.capita.handler.CustomAccessDeniedHandler;
import com.technodev.capita.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final BCryptPasswordEncoder encoder;
    private final CustomAccessDeniedHandler customerAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final CustomAuthorizationFilter customAuthorizationFilter;
    private static final String[] PUBLIC_URLS = {"user/login/**", "user/register/**" ,"/user/verify/code/**","/user/profile","/user/resetpassword/**","/user/verify/password/**"};
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.authorizeHttpRequests(authRequest-> authRequest.requestMatchers(PUBLIC_URLS).permitAll());

        http.authorizeHttpRequests(authRequest-> authRequest.requestMatchers(HttpMethod.DELETE, "/user/delete/**").hasAnyAuthority("DELETE:USER"));

        http.authorizeHttpRequests(authRequest-> authRequest.requestMatchers(HttpMethod.DELETE, "/customer/delete/**").hasAnyAuthority("DELETE:CUSTOMER"));

        http.exceptionHandling(exception-> exception.accessDeniedHandler(customerAccessDeniedHandler).authenticationEntryPoint(customAuthenticationEntryPoint));

        http.authorizeHttpRequests( auth -> auth.anyRequest().authenticated() );

        http.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(){

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

        authenticationProvider.setUserDetailsService(userDetailsService);

        authenticationProvider.setPasswordEncoder(encoder);

        return new ProviderManager(authenticationProvider);

    }
}
