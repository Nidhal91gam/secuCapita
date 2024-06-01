package com.technodev.capita.repository.implementation;

import com.technodev.capita.domain.Role;
import com.technodev.capita.domain.User;
import com.technodev.capita.exception.ApiException;
import com.technodev.capita.repository.RoleRepository;
import com.technodev.capita.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.technodev.capita.enumeration.RoleType.ROLE_USER;
import static com.technodev.capita.enumeration.VerifiacationType.ACCOUNT;
import static com.technodev.capita.query.UserQuery.*;

@Repository

@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User> {


    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository<Role> roleRepository;

    private final BCryptPasswordEncoder encoder;

    @Override
    public User create(User user) {

        //check the email is unique
        if (getEmailCount( user.getEmail().trim().toLowerCase())> 0 ) throw new ApiException("Email already in use. Please use a different email and try again .");

        //save new user
        try{
            KeyHolder holder=new GeneratedKeyHolder();
            SqlParameterSource parameter = getSqlParamaterSource(user);
            jdbc.update(INSERT_USER_QUERY, parameter, holder);
            user.setId(Objects.requireNonNull(holder.getKey()).longValue());
            //Add role to the user
            roleRepository.addRoleToUser(user.getId() , ROLE_USER.name());
            // Send verification Url
            String verificationUrl = getverificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());
            //save URl in verification table
            jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY,Map.of("userId" , user.getId() , "url" , verificationUrl));
            // Send email to User With verification URL
            //emailService.sendverificationUrl(user.getFirstName() , user.getEmail() , verificationUrl , ACCOUNT);
            user.setEnable(false);
            user.setNotLocked(true);
            //Return the newly created user
            return user;
        }catch (EmptyResultDataAccessException exception){
            throw new ApiException("No role found by name:" + ROLE_USER.name());
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }

    //Conter le nombre d'email dans la base de donnée
    private Integer getEmailCount(String email) {
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email" , email) , Integer.class);
    }

    // Mapper le contenu de User a un nouveau Classe SQLParaleter
    private SqlParameterSource getSqlParamaterSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName",user.getFirstName())
                .addValue("lastName",user.getLastName())
                .addValue("email",user.getEmail())
                .addValue("password",encoder.encode(user.getPassword()));
    }

    private String getverificationUrl(String key ,String type){
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + type + "/" + key).toUriString();
    }

    @Override
    public Collection<User> list(int page, int pageSize) {
        return null;
    }

    @Override
    public User get(Long id) {
        return null;
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }
}
