package com.technodev.capita.provider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.technodev.capita.domain.UserPrincipale;
import com.technodev.capita.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;

@Component
@RequiredArgsConstructor
public class TokenProvider {
    private static final String GET_ARRAYS_LLC ="GET_ARRAYS_LLC" ;
    private static final String CUSTOMER_MANAGEMENT_SERVICE = "CUSTOMER_MANAGEMENT_SERVICE";
    public static final String AUTHORITIES = "authorities";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1_800_000;
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 432_000_000;
    @Value("$(jwt.secret)")
    private String secret;
    private final UserService userService;

    public String createAccessToken(UserPrincipale userPrincipale){
        String [] claims = getClaimsFromUser(userPrincipale);
        return JWT.create()
                .withIssuer(GET_ARRAYS_LLC).withAudience(CUSTOMER_MANAGEMENT_SERVICE)
                .withIssuedAt(new Date()).withSubject(userPrincipale.getUsername()).withArrayClaim(AUTHORITIES,claims)
                .withExpiresAt(new Date(currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret.getBytes()));
    }
    public String createRefreshToken(UserPrincipale userPrincipale){

        return JWT.create()
                .withIssuer(GET_ARRAYS_LLC).withAudience(CUSTOMER_MANAGEMENT_SERVICE)
                .withIssuedAt(new Date()).withSubject(userPrincipale.getUsername())
                .withExpiresAt(new Date(currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret.getBytes()));
    }
    public List<GrantedAuthority> getAuthorities(String token){
        String[] claims = getClaimsFromToken(token);
        return stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
    private String[] getClaimsFromUser(UserPrincipale userPrincipale) {
        return userPrincipale.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);
    }
    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier= getJWTVerifier();
        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    private JWTVerifier getJWTVerifier() {
        JWTVerifier verifier;
        try{
            Algorithm algorithm= Algorithm.HMAC512(secret);
            verifier = JWT.require(algorithm).withIssuer(GET_ARRAYS_LLC).build();

        }catch (JWTVerificationException exception){
            throw new JWTVerificationException("Token cannot be verifier");
        }
        return verifier;
    }

    public Authentication getAuthentication(String email, List<GrantedAuthority> authorities, HttpServletRequest request){
        UsernamePasswordAuthenticationToken userPasswordAuthToken = new UsernamePasswordAuthenticationToken(userService.getUserByEmail(email), null,authorities);
        userPasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return userPasswordAuthToken;
    }
    public boolean isTokenValid(String email, String token){
        JWTVerifier verifier= getJWTVerifier();
        return StringUtils.isNotEmpty(email) && !isTokenExpired(verifier, token);

    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }
    public String getSubject(String token, HttpServletRequest request){
        JWTVerifier verifier = getJWTVerifier();
        try{
            return verifier.verify(token).getSubject();
        }catch (TokenExpiredException exception){
            request.setAttribute("expiredMessage", exception.getMessage());
            throw exception;
        }catch (InvalidClaimException exception){
            request.setAttribute("InvalidClaim", exception.getMessage());
            throw exception;
        }catch (Exception exception){
            throw exception;
        }
    }
}
