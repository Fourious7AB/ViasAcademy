package com.neel.StudentAdmission.repo;

import com.neel.StudentAdmission.model.Admission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AdmissionRepository extends MongoRepository<Admission, String> {
    Optional<Admission> findByAdmissionNumber(String admissionNumber);
    
    Optional<Admission> findByAdmissionNumberAndSchoolId(String admissionNumber, String schoolId);
    
    boolean existsByAdmissionNumber(String admissionNumber);
    
    boolean existsByAdmissionNumberAndSchoolId(String admissionNumber, String schoolId);
    
    List<Admission> findByClassNameAndSection(String className, String section);
    
    List<Admission> findByClassNameAndSectionAndSchoolId(String className, String section, String schoolId);
    
    List<Admission> findBySchoolId(String schoolId);
}
