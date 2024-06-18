package com.technodev.capita.resource;

import com.technodev.capita.domain.HttpResponse;
import com.technodev.capita.domain.User;
import com.technodev.capita.domain.UserPrincipale;
import com.technodev.capita.dto.UserDTO;
import com.technodev.capita.exception.ApiException;
import com.technodev.capita.form.LoginForm;
import com.technodev.capita.provider.TokenProvider;
import com.technodev.capita.service.RoleService;
import com.technodev.capita.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

import static com.technodev.capita.dtomapper.UserDTOMapper.toUser;
import static com.technodev.capita.utils.ExceptionUtils.processError;
import static java.time.LocalTime.now;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@RestController
@RequestMapping(path="/user")
@RequiredArgsConstructor
public class UserResource {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RoleService roleService;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private static final String TOKEN_PRIFIX = "Bearer ";

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
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm){
        authenticationManager.authenticate(unauthenticated(loginForm.getEmail() , loginForm.getPassword()));
        Authentication authentication = authenticate(loginForm.getEmail(), loginForm.getPassword());
        UserDTO userDTO= getAuthenticatedUser(authentication);
        //System.out.println(authentication);
        //System.out.println(((UserPrincipale) authentication.getPrincipal()).getUser());
        return userDTO.isUsingMfa() ? sendVerificationCode(userDTO) : sendResponse(userDTO);
    }

    private UserDTO getAuthenticatedUser(Authentication authentication){
        return ((UserPrincipale) authentication.getPrincipal()).getUser();
    }

    private Authentication authenticate(String email, String password){
        try{
            Authentication authentication = authenticationManager.authenticate(unauthenticated(email,password));
            return authentication;
        }catch (Exception exception){
            processError(request ,response, exception);
            throw new ApiException(exception.getMessage());
        }
    }
    private UserPrincipale getUserPrincipal(UserDTO userDTO) {
        return new UserPrincipale(toUser(userService.getUserByEmail(userDTO.getEmail())), roleService.getRoleByUserId(userDTO.getId()));
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
    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO userDTO) {
        userService.sendVerificationCode(userDTO);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user",userDTO))
                        .message("Verification Code Sent")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }
    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toUriString());
    }

    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication){
        UserDTO userDTO = userService.getUserByEmail(authentication.getName());
        System.out.println(authentication);
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

    // START - To reset password when user is not logged in

    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable(name = "email") String email){
        userService.resetPassword(email);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Email sent. Please check your email to reset your password")
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }
    @GetMapping("/verify/password/{key}")
    public ResponseEntity<HttpResponse> verifyPasswordUrl(@PathVariable(name = "key") String key){
        UserDTO userDTO = userService.verifyPasswordKey(key);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userDTO))
                        .message("Please enter a new password")
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

    @PostMapping("/resetpassword/{key}/{password}/{confirmPassword}")
    public ResponseEntity<HttpResponse> resetPasswordWithKey (@PathVariable(name = "key") String key , @PathVariable(name = "password") String password,
                                                          @PathVariable(name = "confirmPassword") String confirmPassword){
        userService.renewPassword(key, password,confirmPassword );
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Password reset successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }


    // END - To reset password when user is not logged in

    @GetMapping("/verify/account/{key}")
    public ResponseEntity<HttpResponse> verifyAccount (@PathVariable(name = "key") String key){

        return ResponseEntity.ok().body(

                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message(userService.verifyAccountKey(key).isEnable() ? "Account already verified" : "Account verified")
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }

    @GetMapping("/refresh/token")
    public ResponseEntity<HttpResponse> refreshToken (HttpServletRequest request){
        if(isHeaderTokenValid(request)){
            String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PRIFIX.length());
            UserDTO userDTO = userService.getUserByEmail(tokenProvider.getSubject(token, request));
            return ResponseEntity.ok().body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .data(Map.of("user",userDTO,
                                    "Access_Token",tokenProvider.createAccessToken(getUserPrincipal(userDTO)),
                                    "Refresh_Token",token))
                            .message("Token refresh")
                            .status(OK)
                            .statusCode(OK.value())
                            .build()
            );
        }else {
            return ResponseEntity.badRequest().body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .reason("Refresh token missing or invalid .")
                            .developerMessage("Refresh token missing or invalid .")
                            .status(BAD_REQUEST)
                            .statusCode(BAD_REQUEST.value())
                            .build());
        }

    }

    private boolean isHeaderTokenValid(HttpServletRequest request) {
        return  request.getHeader(AUTHORIZATION) != null
                && request.getHeader(AUTHORIZATION).startsWith(TOKEN_PRIFIX)
                && tokenProvider.isTokenValid(
                        tokenProvider.getSubject(request.getHeader(AUTHORIZATION).substring(TOKEN_PRIFIX.length()), request),
                        request.getHeader(AUTHORIZATION).substring(TOKEN_PRIFIX.length())
                            );
    }


    @RequestMapping("/error")
    private ResponseEntity<HttpResponse> handleError(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("There is no mapping for a " + request.getMethod() + " request for this path on the server")
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build()
        );
    }
   /* @RequestMapping("/error")
    private ResponseEntity<HttpResponse> handleError1(HttpServletRequest request) {
        return new ResponseEntity<>(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("There is no mapping for a " + request.getMethod() + " request for this path on the server")
                        .status(NOT_FOUND)
                        .statusCode(NOT_FOUND.value())
                        .build(),NO_CONTENT);
    }*/





}
