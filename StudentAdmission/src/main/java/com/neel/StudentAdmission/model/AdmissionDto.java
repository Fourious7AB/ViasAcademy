package com.neel.StudentAdmission.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
@Data
public class AdmissionDto {
    private String id;
    private String schoolId;
    private String studentName;
    private String rollNo;
    private String admissionDate;
    private MultipartFile studentImage;
    private String className;
    private String section;
    private String gender;
    private String bloodGroup;
    private String dob;
    private String religion;
    private String fatherName;
    private String motherName;
    private String phone;
    private String alternativePhone;
    private String email;
    private MultipartFile jointImage;
    private String pincode;
    private String state;
    private String city;
    private String district;
    private String country;
    private String policeStation;
    private String villagePost;
}
