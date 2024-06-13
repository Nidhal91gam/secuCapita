package com.technodev.capita.resource;

import com.technodev.capita.domain.HttpResponse;
import com.technodev.capita.domain.UserPrincipale;
import com.technodev.capita.form.LoginForm;
import com.technodev.capita.domain.User;
import com.technodev.capita.dto.UserDTO;
import com.technodev.capita.provider.TokenProvider;
import com.technodev.capita.service.RoleService;
import com.technodev.capita.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

import static com.technodev.capita.dtomapper.UserDTOMapper.toUser;
import static java.time.LocalTime.now;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(path="/user")
@RequiredArgsConstructor
public class UserResource {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RoleService roleService;

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm){

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginForm.getEmail() , loginForm.getPassword()));

        UserDTO userDTO = userService.getUserByEmail(loginForm.getEmail());
        return userDTO.isUsingMfa() ? sendVerificationCode(userDTO) : sendResponse(userDTO);

    }
    private ResponseEntity<HttpResponse> sendResponse(UserDTO userDTO) {
        return ResponseEntity.ok().body(

                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",userDTO,
                                "Access_Token",tokenProvider.createAccessToken(getUserPrincipal(userDTO)),
                                "Refresh_Token",tokenProvider.createRefreshToken(getUserPrincipal(userDTO))))
                        .message("Login Success")
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }
    private UserPrincipale getUserPrincipal(UserDTO userDTO) {
        return new UserPrincipale(toUser(userService.getUserByEmail(userDTO.getEmail())), roleService.getRoleByUserId(userDTO.getId()).getPermission());
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO userDTO) {
        userService.sendVerificationCode(userDTO);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",userDTO))
                        .message("Verification Code Sent")
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
    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication){

        UserDTO userDTO = userService.getUserByEmail(authentication.getName());
        System.out.println(authentication.getPrincipal());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",userDTO))
                        .message("Profile Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }
    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode (@PathVariable(name = "email") String email , @PathVariable(name = "code") String code){

        UserDTO userDTO = userService.verifyCode(email,code);

        return ResponseEntity.ok().body(

                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",userDTO,
                                "Access_Token",tokenProvider.createAccessToken(getUserPrincipal(userDTO)),
                                "Refresh_Token",tokenProvider.createRefreshToken(getUserPrincipal(userDTO))))
                        .message("Login Success")
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }
    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toUriString());
    }


}
