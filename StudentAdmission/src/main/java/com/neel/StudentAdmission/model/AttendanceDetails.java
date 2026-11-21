package com.neel.StudentAdmission.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Data
@Document(collection = "attendance_details")
public class AttendanceDetails {
    @Id
    private String id;
    private String schoolId; // Added for multi-tenant support
    private String className;
    private String section;
    private Date attendanceDate;
    private String studentName; // Added back as requested
    // Map with roll number as key and attendance status as value
    private Map<String, String> attendance;  // {"101": "P", "102": "A"}
}

