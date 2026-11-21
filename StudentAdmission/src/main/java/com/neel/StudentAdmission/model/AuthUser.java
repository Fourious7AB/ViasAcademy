package com.neel.StudentAdmission.model;



import lombok.Data;

@Data
public class AuthUser {
    private String username; // Admission Number
    private String password; // DOB
    private String role; // Role (e.g., student)
}

