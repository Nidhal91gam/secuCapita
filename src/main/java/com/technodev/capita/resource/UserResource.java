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
    private UserPrincipale getUserPrincipal(UserDTO userDTO) {
        return new UserPrincipale(toUser(userService.getUserByEmail(userDTO.getEmail())), roleService.getRoleByUserId(userDTO.getId()));
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
