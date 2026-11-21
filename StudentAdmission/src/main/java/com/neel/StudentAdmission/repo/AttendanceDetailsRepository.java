package com.neel.StudentAdmission.repo;

import com.neel.StudentAdmission.model.AttendanceDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AttendanceDetailsRepository extends MongoRepository<AttendanceDetails, String> {
    // Legacy methods
    List<AttendanceDetails> findByClassNameAndSection(String className, String section);
    List<AttendanceDetails> findByClassNameAndSectionAndAttendanceDate(String className, String section, Date attendanceDate);
    List<AttendanceDetails> findByClassNameAndSectionAndAttendanceDateBetween(String className, String section, Date startDate, Date endDate);
    
    // School-specific methods
    List<AttendanceDetails> findBySchoolId(String schoolId);
    List<AttendanceDetails> findBySchoolIdAndClassNameAndSection(String schoolId, String className, String section);
    List<AttendanceDetails> findBySchoolIdAndClassNameAndSectionAndAttendanceDate(String schoolId, String className, String section, Date attendanceDate);
    List<AttendanceDetails> findBySchoolIdAndClassNameAndSectionAndAttendanceDateBetween(String schoolId, String className, String section, Date startDate, Date endDate);
}

