package com.Saurabh.Auth_app.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Saurabh.Auth_app.Entity.UserEntity;


public interface UserRepository extends JpaRepository<UserEntity, Long>{

    Optional<UserEntity> findByEmail(String email);

    Boolean existsByEmail(String email);

}
