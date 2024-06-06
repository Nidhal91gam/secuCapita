package com.technodev.capita.resource;

import com.technodev.capita.domain.HttpResponse;
import com.technodev.capita.form.LoginForm;
import com.technodev.capita.domain.User;
import com.technodev.capita.dto.UserDTO;
import com.technodev.capita.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

import static java.time.LocalTime.now;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(path="/user")
@RequiredArgsConstructor
public class UserResource {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm){

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginForm.getEmail() , loginForm.getPassword()));

        UserDTO userDTO = userService.getUserByEmail(loginForm.getEmail());
        return ResponseEntity.ok().body(

                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",userDTO))
                        .message("Login Success")
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user){

        UserDTO userDTO = userService.createUser(user);

        return ResponseEntity.created(getUri()).body(

                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",userDTO))
                        .message("User created")
                        .status(CREATED)
                        .statusCode(CREATED.value())
                        .build()
        );
    }
    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toUriString());
    }


}
