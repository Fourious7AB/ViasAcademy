package com.neel.StudentAdmission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neel.StudentAdmission.model.*;
import com.neel.StudentAdmission.repo.AdmissionRepository;
import com.neel.StudentAdmission.service.AdmissionService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping
public class AdmissionController {
    @Autowired
    private AdmissionService admissionService;
    
    @Autowired
    private AdmissionRepository admissionRepository;
    
    // Legacy endpoint - Get total student count
    @GetMapping("/admissions/total-students")
    public ResponseMessage getTotalStudentCount() {
        try {
            long totalCount = admissionService.getTotalStudentCount();
            return new ResponseMessage("Total student count retrieved successfully", totalCount);
        } catch (Exception e) {
            return new ResponseMessage("Error retrieving total student count: " + e.getMessage(), null);
        }
    }
    
    // Get student count by school
    @GetMapping("/admissions/total-students/{schoolId}")
    public ResponseEntity<ResponseMessage> getStudentCountBySchool(@PathVariable String schoolId) {
        try {
            long totalCount = admissionService.getStudentCountBySchool(schoolId);
            return ResponseEntity.ok(new ResponseMessage("Student count for school retrieved successfully", totalCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage("Error retrieving student count: " + e.getMessage(), null));
        }
    }

//    // Legacy endpoint - Add a new admission
//    @PostMapping("/admissions")
//    public ResponseEntity<ResponseModel<Admission>> addAdmission(@ModelAttribute AdmissionDto admissionDto) {
//        try {
//            // Save the images
//            String filePath = admissionService.saveFile(admissionDto.getStudentImage());
//            String filePath2 = admissionService.saveFile(admissionDto.getJointImage());
//
//            // Create a new Admission object
//            Admission admission = new Admission();
//            admission.setStudentName(admissionDto.getStudentName());
//            admission.setRollNo(admissionDto.getRollNo());
//            admission.setAdmissionDate(admissionDto.getAdmissionDate());
//            admission.setClassName(admissionDto.getClassName());
//            admission.setSection(admissionDto.getSection());
//            admission.setGender(admissionDto.getGender());
//            admission.setBloodGroup(admissionDto.getBloodGroup());
//            admission.setDob(admissionDto.getDob());
//            admission.setReligion(admissionDto.getReligion());
//            admission.setFatherName(admissionDto.getFatherName());
//            admission.setMotherName(admissionDto.getMotherName());
//            admission.setPhone(admissionDto.getPhone());
//            admission.setAlternativePhone(admissionDto.getAlternativePhone());
//            admission.setEmail(admissionDto.getEmail());
//            admission.setPincode(admissionDto.getPincode());
//            admission.setCity(admissionDto.getCity());
//            admission.setState(admissionDto.getState());
//            admission.setDistrict(admissionDto.getDistrict());
//            admission.setCountry(admissionDto.getCountry());
//            admission.setPoliceStation(admissionDto.getPoliceStation());
//            admission.setVillagePost(admissionDto.getVillagePost());
//            admission.setStatus(admission.getStatus());
//            admission.setStudentImage(filePath);
//            admission.setJointImage(filePath2);
//
//            // Use a default schoolId for legacy endpoint
//            Admission createdAdmission = admissionService.createAdmission(admission, "67ca9167d56ec91fc9539234");
//
//            return ResponseEntity.ok(new ResponseModel<>("Student added successfully!", createdAdmission));
//        } catch (IOException e) {
//            return ResponseEntity.status(500).body(new ResponseModel<>("Error saving admission: " + e.getMessage(), null));
//        }
//    }
    
    // Add a new admission with schoolId
    @PostMapping("/admissions/{schoolId}")
    public ResponseEntity<ResponseModel<Admission>> addAdmissionWithSchool(
            @PathVariable String schoolId, @ModelAttribute AdmissionDto admissionDto) {
        System.out.println("School id"+schoolId);
        try {
            // Save the images
            String filePath = admissionService.saveFile(admissionDto.getStudentImage());
            String filePath2 = admissionService.saveFile(admissionDto.getJointImage());

            // Create a new Admission object
            Admission admission = new Admission();
            admission.setStudentName(admissionDto.getStudentName());
            admission.setRollNo(admissionDto.getRollNo());
            admission.setAdmissionDate(admissionDto.getAdmissionDate());
            admission.setClassName(admissionDto.getClassName());
            admission.setSection(admissionDto.getSection());
            admission.setGender(admissionDto.getGender());
            admission.setBloodGroup(admissionDto.getBloodGroup());
            admission.setDob(admissionDto.getDob());
            admission.setReligion(admissionDto.getReligion());
            admission.setFatherName(admissionDto.getFatherName());
            admission.setMotherName(admissionDto.getMotherName());
            admission.setPhone(admissionDto.getPhone());
            admission.setAlternativePhone(admissionDto.getAlternativePhone());
            admission.setEmail(admissionDto.getEmail());
            admission.setPincode(admissionDto.getPincode());
            admission.setCity(admissionDto.getCity());
            admission.setState(admissionDto.getState());
            admission.setDistrict(admissionDto.getDistrict());
            admission.setCountry(admissionDto.getCountry());
            admission.setPoliceStation(admissionDto.getPoliceStation());
            admission.setVillagePost(admissionDto.getVillagePost());
            admission.setStatus(admission.getStatus());
            admission.setStudentImage(filePath);
            admission.setJointImage(filePath2);

            // Save the admission in the database with schoolId
            Admission createdAdmission = admissionService.createAdmission(admission, schoolId);

            return ResponseEntity.ok(new ResponseModel<>("Student added successfully!", createdAdmission));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(new ResponseModel<>("Error saving admission: " + e.getMessage(), null));
        }
    }
    
    // Legacy endpoint - Validate admission by number
    @GetMapping("/admissions/validate/{admissionNumber}")
    public ResponseEntity<Boolean> validateAdmission(@PathVariable String admissionNumber) {
        boolean exists = admissionRepository.existsByAdmissionNumber(admissionNumber);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }
    
    // Validate admission by number and schoolId
    @GetMapping("/admissions/validate/{admissionNumber}/{schoolId}")
    public ResponseEntity<Boolean> validateAdmissionWithSchool(
            @PathVariable String schoolId, @PathVariable String admissionNumber) {
        boolean exists = admissionRepository.existsByAdmissionNumberAndSchoolId(admissionNumber, schoolId);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }
    
    // Legacy endpoint - Get all Admissions
    @GetMapping("/admissions")
    public ResponseMessage getAllAdmissions() {
        Iterable<Admission> admissions = admissionService.getAllAdmissions();
        return new ResponseMessage("Admissions retrieved successfully", admissions);
    }
    
    // Get all Admissions by schoolId
    @GetMapping("/admissions/{schoolId}")
    public ResponseEntity<ResponseMessage> getAdmissionsBySchoolId(@PathVariable String schoolId) {
        List<Admission> admissions = admissionService.getAdmissionsBySchoolId(schoolId);
        
        if (admissions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseMessage("No admissions found for this school", null));
        }
        
        return ResponseEntity.ok(new ResponseMessage("Admissions retrieved successfully", admissions));
    }

    // Legacy endpoint - Get Admission by Admission Number
    @GetMapping("/admissions/number/{admissionNumber}")
    public ResponseMessage getAdmissionByNumber(@PathVariable String admissionNumber) {
        Optional<Admission> admission = admissionService.getAdmissionByNumber(admissionNumber);
        return admission.map(value -> new ResponseMessage("Admission retrieved successfully", value))
                .orElseGet(() -> new ResponseMessage("Admission not found with admission number: " + admissionNumber, null));
    }
    
    // Get Admission by Admission Number and schoolId
    @GetMapping("/admissions/number/{admissionNumber}/{schoolId}")
    public ResponseEntity<ResponseMessage> getAdmissionByNumberAndSchool(
            @PathVariable String schoolId, @PathVariable String admissionNumber) {
        
        Optional<Admission> admission = admissionService.getAdmissionByNumberAndSchool(admissionNumber, schoolId);
        
        if (admission.isPresent()) {
            return ResponseEntity.ok(new ResponseMessage("Admission retrieved successfully", admission.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseMessage("Admission not found with number: " + admissionNumber + " for this school", null));
        }
    }

    // Legacy endpoint - Update Admission by Admission Number
    @PutMapping("/admissions/number/{admissionNumber}")
    public ResponseMessage updateAdmissionByNumber(
            @PathVariable String admissionNumber, @RequestBody Admission updatedAdmission) {
        try {
            Admission admission = admissionService.updateAdmissionByNumber(admissionNumber, updatedAdmission);
            return new ResponseMessage("Admission updated successfully", admission);
        } catch (Exception e) {
            return new ResponseMessage(e.getMessage(), null);
        }
    }
    
    // Update Admission by Admission Number and schoolId
    @PutMapping("/admissions/number/{admissionNumber}/{schoolId}")
    public ResponseEntity<ResponseMessage> updateAdmissionByNumberAndSchool(
            @PathVariable String schoolId,
            @PathVariable String admissionNumber,
            @RequestBody Admission updatedAdmission) {
        
        try {
            Admission admission = admissionService.updateAdmissionByNumberAndSchool(admissionNumber, schoolId, updatedAdmission);
            return ResponseEntity.ok(new ResponseMessage("Admission updated successfully", admission));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseMessage(e.getMessage(), null));
        }
    }

    // Legacy endpoint - Delete Admission by Admission Number
    @DeleteMapping("/admissions/number/{admissionNumber}")
    public ResponseMessage deleteAdmissionByNumber(@PathVariable String admissionNumber) {
        try {
            admissionService.deleteAdmissionByNumber(admissionNumber);
            return new ResponseMessage("Admission deleted successfully", null);
        } catch (Exception e) {
            return new ResponseMessage(e.getMessage(), null);
        }
    }
    
    // Delete Admission by Admission Number and schoolId
    @DeleteMapping("/admissions/number/{admissionNumber}/{schoolId}")
    public ResponseEntity<ResponseMessage> deleteAdmissionByNumberAndSchool(
            @PathVariable String schoolId, @PathVariable String admissionNumber) {
        
        boolean isDeleted = admissionService.deleteAdmissionByNumberAndSchool(admissionNumber, schoolId);
        
        if (isDeleted) {
            return ResponseEntity.ok(new ResponseMessage("Admission deleted successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseMessage("Admission not found with number: " + admissionNumber + " for this school", null));
        }
    }
    
    // Legacy endpoint - Generate and download ID card
    @GetMapping("/admissions/generate-id-card/{admissionNumber}")
    public ResponseEntity<Resource> generateAndDownloadIDCard(@PathVariable String admissionNumber) {
        // Generate the ID card and get the file path
        String filePath = admissionService.generateAndSaveIDCard(admissionNumber);

        // Prepare the file for download
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("Failed to generate ID card.");
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }
    
    // Generate and download ID card with schoolId validation
    @GetMapping("/admissions/generate-id-card/{admissionNumber}/{schoolId}")
    public ResponseEntity<?> generateAndDownloadIDCardWithSchool(
            @PathVariable String schoolId, @PathVariable String admissionNumber) {
        
        try {
            // Generate the ID card and get the file path
            String filePath = admissionService.generateAndSaveIDCardWithSchool(admissionNumber, schoolId);
    
            // Prepare the file for download
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ResponseMessage("Failed to generate ID card: File not found after generation", null));
            }
    
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage("Error generating ID card: " + e.getMessage(), null));
        }
    }

    // Legacy endpoint - Update admission status
    @PutMapping("/admissions/updateStatus")
    public ResponseEntity<ResponseMessage> updateAdmissionStatus(@RequestBody Map<String, String> request) {
        String admissionNumber = request.get("admissionNumber");
        String status = request.get("status");

        // Find the admission by admissionNumber
        Optional<Admission> admissionOpt = admissionRepository.findByAdmissionNumber(admissionNumber);

        if (admissionOpt.isPresent()) {
            Admission admission = admissionOpt.get();
            admission.setStatus(status); // Update the status
            admissionRepository.save(admission); // Save updated admission

            return ResponseEntity.ok(new ResponseMessage("Admission status updated successfully", admission));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseMessage("Error: Admission not found", null));
        }
    }
    
    // Update admission status with schoolId validation
    @PutMapping("/admissions/updateStatus/{schoolId}")
    public ResponseEntity<ResponseMessage> updateAdmissionStatusWithSchool(
            @PathVariable String schoolId, @RequestBody Map<String, String> request) {
        System.out.println("from api"+schoolId);
        System.out.println("request"+request);
        
        String admissionNumber = request.get("admissionNumber");
        System.out.println(admissionNumber);
        String status = request.get("status");
        System.out.println(status);
        try {
            Admission updatedAdmission = admissionService.updateAdmissionStatus(admissionNumber, schoolId, status);
            return ResponseEntity.ok(new ResponseMessage("Admission status updated successfully", updatedAdmission));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseMessage("Error: " + e.getMessage(), null));
        }
    }
    
    // Legacy endpoint - Get admissions by class and section
    @GetMapping("/admissions/class/{className}/section/{section}")
    public ResponseEntity<ResponseMessage> getAdmissionsByClassAndSection(
            @PathVariable String className, @PathVariable String section) {
        
        List<Admission> admissions = admissionRepository.findByClassNameAndSection(className, section);
        
        if (admissions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseMessage("No admissions found for this class and section", null));
        }
        
        return ResponseEntity.ok(new ResponseMessage("Admissions retrieved successfully", admissions));
    }
    
    // Get admissions by class, section and schoolId
    @GetMapping("/{schoolId}/admissions/class/{className}/section/{section}")
    public ResponseEntity<ResponseMessage> getAdmissionsByClassSectionAndSchool(
            @PathVariable String schoolId,
            @PathVariable String className,
            @PathVariable String section) {
        
        List<Admission> admissions = admissionRepository.findByClassNameAndSectionAndSchoolId(className, section, schoolId);
        
        if (admissions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseMessage("No admissions found for this class, section and school", null));
        }
        
        return ResponseEntity.ok(new ResponseMessage("Admissions retrieved successfully", admissions));
    }
}
