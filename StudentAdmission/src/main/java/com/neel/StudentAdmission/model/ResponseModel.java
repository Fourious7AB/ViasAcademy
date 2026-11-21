package com.neel.StudentAdmission.model;



import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseModel<T> {
    private String message;
    private T data;
}

