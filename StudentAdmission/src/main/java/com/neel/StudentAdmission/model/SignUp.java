package com.neel.StudentAdmission.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "sign_up")
@NoArgsConstructor
@AllArgsConstructor
public class SignUp {
    @Id
    private String id;
    private String userName;
    private String email;
    private String phoneNumber;
    private String schoolName;
    private String address;
    private String instituteType;
    private String password;

}
