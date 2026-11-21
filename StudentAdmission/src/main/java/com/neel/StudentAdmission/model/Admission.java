package com.neel.StudentAdmission.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "admissions")
public class Admission {
    @Id
    private String id;
    private String schoolId;
    private String admissionNumber; // Format: schoolCode/year/autoIncrementedId
    private String studentName;
    private String rollNo;
    private String admissionDate;
    private String studentImage;
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
    private String jointImage;
    private String pincode;
    private String state;
    private String city;
    private String district;
    private String country;
    private String policeStation;
    private String villagePost;
    private String idCardPath;
    private String status="pending";
}

