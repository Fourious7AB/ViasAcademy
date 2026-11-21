package com.neel.StudentAdmission.repo;

import com.neel.StudentAdmission.model.School;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SchoolRepository extends MongoRepository<School, String> {
    Optional<School> findBySchoolId(String schoolId);
}