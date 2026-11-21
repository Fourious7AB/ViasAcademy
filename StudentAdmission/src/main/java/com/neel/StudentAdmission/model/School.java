package com.neel.StudentAdmission.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "schools")
public class School {
    @Id
    private String id;
    private String schoolId;
    private String schoolName;
    private String schoolRegistrationNumber;
    private String schoolAddress;
    private String schoolEmail;
    private String schoolPhone;
    private String schoolDescription;
    private String schoolLogo; // File path
    private String schoolImage; // File path
    private String schoolCode;
    private String schoolPrincipalName;
    private String schoolPrincipalEmail;
    private String schoolPrincipalPhone;

}

