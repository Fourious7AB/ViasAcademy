package com.neel.StudentAdmission.controller;

import com.neel.StudentAdmission.model.Admission;
import com.neel.StudentAdmission.model.AttendanceDetails;
import com.neel.StudentAdmission.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    // Legacy endpoint - Get students by class and section
    @GetMapping("/attendance/students")
    public ResponseEntity<List<Admission>> getStudentsByClassAndSection(
            @RequestParam String className,
            @RequestParam String section) {
        List<Admission> students = attendanceService.getStudentsByClassAndSection(className, section);
        return ResponseEntity.ok(students);
    }
    
    // Get students by class, section and school
    @GetMapping("/{schoolId}/attendance/students")
    public ResponseEntity<List<Admission>> getStudentsByClassSectionAndSchool(
            @PathVariable String schoolId,
            @RequestParam String className,
            @RequestParam String section) {
        List<Admission> students = attendanceService.getStudentsByClassSectionAndSchool(className, section, schoolId);
        return ResponseEntity.ok(students);
    }
    
    // Legacy endpoint - Get attendance by class and section
    @GetMapping("/attendance/record")
    public ResponseEntity<List<AttendanceDetails>> getAttendanceRecords(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date attendanceDate) {

        List<AttendanceDetails> attendanceRecords;
        if (attendanceDate != null) {
            attendanceRecords = attendanceService.getAttendanceByClassSectionAndDate(className, section, attendanceDate);
        } else {
            attendanceRecords = attendanceService.getAttendanceByClassAndSection(className, section);
        }
        return ResponseEntity.ok(attendanceRecords);
    }
    
    // Get attendance by class, section and school
    @GetMapping("/attendance/record/{schoolId}")
    public ResponseEntity<List<AttendanceDetails>> getAttendanceRecordsBySchool(
            @PathVariable String schoolId,
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date attendanceDate) {

        List<AttendanceDetails> attendanceRecords;
        if (attendanceDate != null) {
            attendanceRecords = attendanceService.getAttendanceByClassSectionDateAndSchool(className, section, attendanceDate, schoolId);
        } else {
            attendanceRecords = attendanceService.getAttendanceByClassSectionAndSchool(className, section, schoolId);
        }
        return ResponseEntity.ok(attendanceRecords);
    }

    // Legacy endpoint - Record attendance
    @PostMapping("/attendance/record")
    public ResponseEntity<String> recordAttendance(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate attendanceDate,
            @RequestBody Map<String, String> attendance) {
        Date convertedDate = Date.from(attendanceDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String response = attendanceService.saveAttendance(className, section, convertedDate, attendance);
        return ResponseEntity.ok(response);
    }
    
    // Record attendance with school
    @PostMapping("/{schoolId}/attendance/record")
    public ResponseEntity<String> recordAttendanceWithSchool(
            @PathVariable String schoolId,
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate attendanceDate,
            @RequestBody Map<String, String> attendance) {
        Date convertedDate = Date.from(attendanceDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String response = attendanceService.saveAttendanceWithSchool(className, section, convertedDate, attendance, schoolId);
        return ResponseEntity.ok(response);
    }

    // Legacy endpoint - Get student attendance
    @GetMapping("/attendance/student-attendance")
    public ResponseEntity<List<Map<String, String>>> getStudentAttendance(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam String month) {

        // Parse the month parameter to get start and end dates
        Date[] dateRange = getStartEndDateOfMonth(month);
        if (dateRange == null) {
            return ResponseEntity.badRequest().body(null);  // Invalid month format
        }

        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        // Call the service method to fetch attendance data
        Map<String, Map<String, String>> attendanceData = attendanceService
                .getStudentAttendanceByClassAndSection(className, section, startDate, endDate);

        // Convert the Map to List of Maps (Array format)
        List<Map<String, String>> responseArray = convertAttendanceToArray(attendanceData);

        return ResponseEntity.ok(responseArray);
    }
    
    // Get student attendance with school
    @GetMapping("/{schoolId}/attendance/student-attendance")
    public ResponseEntity<List<Map<String, String>>> getStudentAttendanceWithSchool(
            @PathVariable String schoolId,
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam String month) {

        // Parse the month parameter to get start and end dates
        Date[] dateRange = getStartEndDateOfMonth(month);
        if (dateRange == null) {
            return ResponseEntity.badRequest().body(null);  // Invalid month format
        }

        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        // Call the service method to fetch attendance data with school ID
        Map<String, Map<String, String>> attendanceData = attendanceService
                .getStudentAttendanceByClassSectionAndSchool(className, section, startDate, endDate, schoolId);

        // Convert the Map to List of Maps (Array format)
        List<Map<String, String>> responseArray = convertAttendanceToArray(attendanceData);

        return ResponseEntity.ok(responseArray);
    }

    // Helper method to convert Map to List (Array format)
    private List<Map<String, String>> convertAttendanceToArray(Map<String, Map<String, String>> attendanceData) {
        List<Map<String, String>> attendanceList = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> entry : attendanceData.entrySet()) {
            Map<String, String> studentAttendance = new LinkedHashMap<>();
            studentAttendance.put("rollNo", entry.getKey());

            // Adding attendance data (date -> attendance value)
            for (Map.Entry<String, String> dateEntry : entry.getValue().entrySet()) {
                studentAttendance.put(dateEntry.getKey(), dateEntry.getValue());
            }

            attendanceList.add(studentAttendance);
        }

        return attendanceList;
    }

    // Helper method to parse the month and return start and end dates
    private Date[] getStartEndDateOfMonth(String month) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");
            Date parsedMonth = dateFormat.parse(month);  // Parse the month in "MM/yyyy" format

            // Set start date to the 1st of the month
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(parsedMonth);
            startCalendar.set(Calendar.DAY_OF_MONTH, 1);
            Date startDate = startCalendar.getTime();

            // Set end date to the last day of the month
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(parsedMonth);
            endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date endDate = endCalendar.getTime();

            return new Date[] { startDate, endDate };
        } catch (ParseException e) {
            e.printStackTrace();
            return null;  // Return null if parsing fails
        }
    }
    
    // Legacy endpoint - Get formatted student attendance
    @GetMapping("/attendance/student-attendance1")
    public ResponseEntity<List<Map<String, Object>>> getFormattedStudentAttendance(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam String month) {

        List<Map<String, Object>> response = attendanceService.getFormattedAttendanceByClassAndSection(
                className, section, month);

        return ResponseEntity.ok(response);
    }
    
    // Get formatted student attendance with school
    @GetMapping("/{schoolId}/attendance/student-attendance1")
    public ResponseEntity<List<Map<String, Object>>> getFormattedStudentAttendanceWithSchool(
            @PathVariable String schoolId,
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam String month) {

        List<Map<String, Object>> response = attendanceService.getFormattedAttendanceByClassSectionAndSchool(
                className, section, month, schoolId);

        return ResponseEntity.ok(response);
    }
}