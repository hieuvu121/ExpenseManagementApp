package com.be9expensphie.expensphie_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.be9expensphie.expensphie_backend.entity.ForgotPasswordEntity;
import com.be9expensphie.expensphie_backend.entity.UserEntity;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPasswordEntity, Long>{
    @Query("select fp from ForgotPasswordEntity fp where fp.otp = ?1 and fp.userEntity = ?2")
    Optional<ForgotPasswordEntity> findByOtpAndUserEntity(Integer otp, UserEntity userEntity);
}
