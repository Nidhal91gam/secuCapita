package com.technodev.capita.repository;

import com.technodev.capita.domain.User;

import java.util.Collection;

public interface UserRepository <T extends User> {

    /* Basic crud Operation */
    T create ( T data );

    Collection<T> list (int page , int pageSize);

    T get(Long id);

    T update(T data);

    Boolean delete(Long id);

    /* more complex operation */






}
