package com.neel.StudentAdmission.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SchoolDTO {
    private String schoolName;
    private String schoolCode;
    private MultipartFile schoolLogo;
    private MultipartFile schoolImage;
    private String schoolRegistrationNumber;
    private String schoolAddress;
    private String schoolEmail;
    private String schoolPhone;
    private String schoolDescription;
    private String schoolPrincipalName;
    private String schoolPrincipalEmail;
    private String schoolPrincipalPhone;
}
