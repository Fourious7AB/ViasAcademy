package com.neel.StudentAdmission.service;

import com.neel.StudentAdmission.model.Admission;
import com.neel.StudentAdmission.model.AttendanceDetails;
import com.neel.StudentAdmission.repo.AdmissionRepository;
import com.neel.StudentAdmission.repo.AttendanceDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class AttendanceService {
    @Autowired
    private AdmissionRepository admissionRepository;

    @Autowired
    private AttendanceDetailsRepository attendanceDetailsRepository;
    
    // Legacy methods
    public List<Admission> getStudentsByClassAndSection(String className, String section) {
        return admissionRepository.findByClassNameAndSection(className, section);
    }
    
    public List<AttendanceDetails> getAttendanceByClassAndSection(String className, String section) {
        return attendanceDetailsRepository.findByClassNameAndSection(className, section);
    }
    
    public List<AttendanceDetails> getAttendanceByClassSectionAndDate(String className, String section, Date attendanceDate) {
        return attendanceDetailsRepository.findByClassNameAndSectionAndAttendanceDate(className, section, attendanceDate);
    }

    public String saveAttendance(String className, String section, Date attendanceDate, Map<String, String> attendance) {
        AttendanceDetails attendanceDetails = new AttendanceDetails();
        attendanceDetails.setClassName(className);
        attendanceDetails.setSection(section);
        attendanceDetails.setAttendanceDate(attendanceDate);
        attendanceDetails.setAttendance(attendance);
        // Default schoolId for legacy method
        attendanceDetails.setSchoolId("67ca9167d56ec91fc9539234");

        attendanceDetailsRepository.save(attendanceDetails);
        return "Attendance saved successfully.";
    }
    
    // School-specific methods
    public List<Admission> getStudentsByClassSectionAndSchool(String className, String section, String schoolId) {
        return admissionRepository.findByClassNameAndSectionAndSchoolId(className, section, schoolId);
    }
    
    public List<AttendanceDetails> getAttendanceByClassSectionAndSchool(String className, String section, String schoolId) {
        return attendanceDetailsRepository.findBySchoolIdAndClassNameAndSection(schoolId, className, section);
    }
    
    public List<AttendanceDetails> getAttendanceByClassSectionDateAndSchool(String className, String section, Date attendanceDate, String schoolId) {
        return attendanceDetailsRepository.findBySchoolIdAndClassNameAndSectionAndAttendanceDate(schoolId, className, section, attendanceDate);
    }

    public String saveAttendanceWithSchool(String className, String section, Date attendanceDate, Map<String, String> attendance, String schoolId) {
        AttendanceDetails attendanceDetails = new AttendanceDetails();
        attendanceDetails.setClassName(className);
        attendanceDetails.setSection(section);
        attendanceDetails.setAttendanceDate(attendanceDate);
        attendanceDetails.setAttendance(attendance);
        attendanceDetails.setSchoolId(schoolId);

        attendanceDetailsRepository.save(attendanceDetails);
        return "Attendance saved successfully.";
    }
    
    // Legacy method for student attendance
    public Map<String, Map<String, String>> getStudentAttendanceByClassAndSection(
            String className, String section, Date startDate, Date endDate) {

        // Fetch attendance records for the class and section within the date range
        List<AttendanceDetails> attendanceRecords = attendanceDetailsRepository
                .findByClassNameAndSectionAndAttendanceDateBetween(className, section, startDate, endDate);

        return processAttendanceRecords(attendanceRecords, startDate, endDate);
    }
    
    // School-specific method for student attendance
    public Map<String, Map<String, String>> getStudentAttendanceByClassSectionAndSchool(
            String className, String section, Date startDate, Date endDate, String schoolId) {

        // Fetch attendance records for the class, section, and school within the date range
        List<AttendanceDetails> attendanceRecords = attendanceDetailsRepository
                .findBySchoolIdAndClassNameAndSectionAndAttendanceDateBetween(schoolId, className, section, startDate, endDate);

        return processAttendanceRecords(attendanceRecords, startDate, endDate);
    }
    
    // Helper method to process attendance records
    private Map<String, Map<String, String>> processAttendanceRecords(
            List<AttendanceDetails> attendanceRecords, Date startDate, Date endDate) {
        
        // Map of rollNo -> (date -> attendance)
        Map<String, Map<Date, String>> studentAttendanceMap = new HashMap<>();

        // Generate a list of all dates in the range
        List<Date> allDates = getDatesInRange(startDate, endDate);
        Collections.sort(allDates);

        // Initialize attendance map with default values for all students
        for (AttendanceDetails record : attendanceRecords) {
            Date dateKey = record.getAttendanceDate();

            // Check if the attendance date is within the specified range
            if (!dateKey.before(startDate) && !dateKey.after(endDate)) {
                for (String rollNo : record.getAttendance().keySet()) {
                    // Ensure a map exists for this roll number
                    studentAttendanceMap.computeIfAbsent(rollNo, k -> new TreeMap<>());

                    // Fetch actual attendance status (P or A) for this date and roll number
                    String attendanceValue = record.getAttendance().get(rollNo);
                    studentAttendanceMap.get(rollNo).put(dateKey, attendanceValue);
                }
            }
        }

        // Initialize default attendance values for missing entries
        for (String rollNo : studentAttendanceMap.keySet()) {
            for (Date date : allDates) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                // Default to "S" for Sundays, else "-" for other days
                String attendanceValue = (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) ? "S" : "-";

                // Put the default attendance value only if no attendance exists for this date
                studentAttendanceMap.get(rollNo).putIfAbsent(date, attendanceValue);
            }
        }

        // Convert dates to string keys in the response
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Map<String, String>> finalResponse = new HashMap<>();
        for (Map.Entry<String, Map<Date, String>> entry : studentAttendanceMap.entrySet()) {
            Map<String, String> sortedAttendanceData = new LinkedHashMap<>();
            for (Map.Entry<Date, String> dateEntry : entry.getValue().entrySet()) {
                sortedAttendanceData.put(dateFormat.format(dateEntry.getKey()), dateEntry.getValue());
            }
            finalResponse.put(entry.getKey(), sortedAttendanceData);
        }

        return finalResponse;
    }

    // Method to get all dates between startDate and endDate
    public List<Date> getDatesInRange(Date startDate, Date endDate) {
        List<Date> datesInRange = new ArrayList<>();

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);

        while (!startCalendar.after(endCalendar)) {
            datesInRange.add(startCalendar.getTime());
            startCalendar.add(Calendar.DATE, 1); // Increment by 1 day
        }

        return datesInRange;
    }
    
    // Legacy method for formatted attendance
    public List<Map<String, Object>> getFormattedAttendanceByClassAndSection(
            String className, String section, String monthYear) {

        // Parse the month and year from the request parameter
        String[] monthYearParts = monthYear.split("/");
        int month = Integer.parseInt(monthYearParts[0]);
        int year = Integer.parseInt(monthYearParts[1]);

        // Calculate the date range for the given month
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        // Fetch attendance data
        Map<String, Map<String, String>> attendanceData = getStudentAttendanceByClassAndSection(
                className, section, 
                Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        return formatAttendanceResponse(attendanceData);
    }
    
    // School-specific method for formatted attendance
    public List<Map<String, Object>> getFormattedAttendanceByClassSectionAndSchool(
            String className, String section, String monthYear, String schoolId) {

        // Parse the month and year from the request parameter
        String[] monthYearParts = monthYear.split("/");
        int month = Integer.parseInt(monthYearParts[0]);
        int year = Integer.parseInt(monthYearParts[1]);

        // Calculate the date range for the given month
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        // Fetch attendance data with school ID
        Map<String, Map<String, String>> attendanceData = getStudentAttendanceByClassSectionAndSchool(
                className, section, 
                Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                schoolId);

        return formatAttendanceResponse(attendanceData);
    }
    
    // Helper method to format attendance response
    private List<Map<String, Object>> formatAttendanceResponse(Map<String, Map<String, String>> attendanceData) {
        // Transform the data to include 'attendance' as a nested object
        List<Map<String, Object>> formattedResponse = new ArrayList<>();
        attendanceData.forEach((rollNo, attendanceMap) -> {
            Map<String, Object> studentAttendance = new HashMap<>();
            studentAttendance.put("rollNo", rollNo);
            studentAttendance.put("attendance", attendanceMap);
            formattedResponse.add(studentAttendance);
        });

        return formattedResponse;
    }

    // Legacy method for roll number and attendance
    public Map<String, Map<String, String>> getRollNoAndAttendanceByClassAndSection(
            String className, String section, Date startDate, Date endDate) {

        // Fetch attendance records for the class and section within the date range
        List<AttendanceDetails> attendanceRecords = attendanceDetailsRepository
                .findByClassNameAndSectionAndAttendanceDateBetween(className, section, startDate, endDate);

        return processRollNoAndAttendance(attendanceRecords, startDate, endDate);
    }
    
    // School-specific method for roll number and attendance
    public Map<String, Map<String, String>> getRollNoAndAttendanceByClassSectionAndSchool(
            String className, String section, Date startDate, Date endDate, String schoolId) {

        // Fetch attendance records for the class, section, and school within the date range
        List<AttendanceDetails> attendanceRecords = attendanceDetailsRepository
                .findBySchoolIdAndClassNameAndSectionAndAttendanceDateBetween(schoolId, className, section, startDate, endDate);

        return processRollNoAndAttendance(attendanceRecords, startDate, endDate);
    }
    
    // Helper method to process roll number and attendance
    private Map<String, Map<String, String>> processRollNoAndAttendance(
            List<AttendanceDetails> attendanceRecords, Date startDate, Date endDate) {
        
        // Map of rollNo -> (date -> attendance)
        Map<String, Map<Date, String>> studentAttendanceMap = new HashMap<>();

        // Generate a list of all dates in the range
        List<Date> allDates = getDatesInRange(startDate, endDate);

        // Initialize attendance map with default values for all students
        for (AttendanceDetails record : attendanceRecords) {
            Date dateKey = record.getAttendanceDate();

            // Check if the attendance date is within the specified range
            if (!dateKey.before(startDate) && !dateKey.after(endDate)) {
                for (String rollNo : record.getAttendance().keySet()) {
                    // Ensure a map exists for this roll number
                    studentAttendanceMap.computeIfAbsent(rollNo, k -> new HashMap<>());

                    // Fetch actual attendance status (P or A) for this date and roll number
                    String attendanceValue = record.getAttendance().get(rollNo);
                    studentAttendanceMap.get(rollNo).put(dateKey, attendanceValue);
                }
            }
        }

        // Initialize default attendance values for missing entries
        for (String rollNo : studentAttendanceMap.keySet()) {
            for (Date date : allDates) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                // Default to "S" for Sundays, else "-"
                String attendanceValue = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? "S" : "-";

                // Put the default attendance value only if no attendance exists for this date
                studentAttendanceMap.get(rollNo).putIfAbsent(date, attendanceValue);
            }
        }

        // Convert the map with Date keys to a map with String keys
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Map<String, String>> responseMap = new HashMap<>();
        for (Map.Entry<String, Map<Date, String>> entry : studentAttendanceMap.entrySet()) {
            Map<String, String> attendanceMap = new LinkedHashMap<>();
            for (Map.Entry<Date, String> attendanceEntry : entry.getValue().entrySet()) {
                attendanceMap.put(dateFormat.format(attendanceEntry.getKey()), attendanceEntry.getValue());
            }
            responseMap.put(entry.getKey(), attendanceMap);
        }

        return responseMap;
    }


}

