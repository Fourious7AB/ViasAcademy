package com.neel.StudentAdmission.repo;

import com.neel.StudentAdmission.model.SignUp;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SignUpRepository extends MongoRepository<SignUp, String> {
    Optional<SignUp> findByEmail(String email);
    Optional<SignUp> findByPhoneNumber(String phoneNumber);
}

