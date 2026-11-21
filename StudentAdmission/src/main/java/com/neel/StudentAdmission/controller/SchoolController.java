package com.neel.StudentAdmission.controller;

import com.neel.StudentAdmission.model.ResponseModel;
import com.neel.StudentAdmission.model.School;
import com.neel.StudentAdmission.model.SchoolDTO;
import com.neel.StudentAdmission.service.SchoolService;
import jakarta.annotation.Resource;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/schools")
public class SchoolController {

    private final SchoolService schoolService;

    @Autowired
    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    // Add a new school with logo
    @PostMapping(value = "/{schoolId}",consumes = "multipart/form-data")
    public ResponseEntity<ResponseModel<School>> addSchool(@PathVariable String schoolId,@ModelAttribute SchoolDTO schoolDTO) {
        try {
            // Save the logo file
            String filePath = schoolService.saveFile(schoolDTO.getSchoolLogo());
            String filePath2 = schoolService.saveFile(schoolDTO.getSchoolImage());

            // Create a new School object
            School school = new School();
            school.setSchoolId(schoolId);
            school.setSchoolName(schoolDTO.getSchoolName());
            school.setSchoolCode(schoolDTO.getSchoolCode());
            school.setSchoolRegistrationNumber(schoolDTO.getSchoolRegistrationNumber());
            school.setSchoolAddress(schoolDTO.getSchoolAddress());
            school.setSchoolEmail(schoolDTO.getSchoolEmail());
            school.setSchoolPhone(schoolDTO.getSchoolPhone());
            school.setSchoolDescription(schoolDTO.getSchoolDescription());
            school.setSchoolPrincipalName(schoolDTO.getSchoolPrincipalName());
            school.setSchoolPrincipalPhone(schoolDTO.getSchoolPrincipalPhone());
            school.setSchoolPrincipalEmail(schoolDTO.getSchoolPrincipalEmail());

            school.setSchoolLogo(filePath);
            school.setSchoolImage(filePath2);

            // Save the school in the database
            School savedSchool = schoolService.saveSchool(school);

            return ResponseEntity.ok(new ResponseModel<>("School added successfully!", savedSchool));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(new ResponseModel<>("Error saving school: " + e.getMessage(), null));
        }
    }

    // Get school details by ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<School>> getSchoolById(@PathVariable String id) {
        School school = schoolService.getSchoolById(id);
        if (school != null) {
            return ResponseEntity.ok(new ResponseModel<>("School details found!", school));
        } else {
            return ResponseEntity.status(404).body(new ResponseModel<>("School not found!", null));
        }
    }

    // Get all schools
    @GetMapping
    public ResponseEntity<ResponseModel<List<School>>> getAllSchools() {
        List<School> schools = schoolService.getAllSchools();
        return ResponseEntity.ok(new ResponseModel<>("Schools retrieved successfully!", schools));
    }

//    // Update school details by ID
//    @PutMapping("/{id}")
//    public ResponseEntity<ResponseMessage<School>> updateSchool(@PathVariable String id, @RequestBody SchoolDTO schoolDTO) {
//        try {
//            School school = schoolService.getSchoolById(id);
//            if (school != null) {
//                school.setSchoolName(schoolDTO.getSchoolName());
//                school.setSchoolCode(schoolDTO.getSchoolCode());
//                if (schoolDTO.getSchoolLogo() != null) {
//                    String filePath = schoolService.saveFile(schoolDTO.getSchoolLogo());
//                    school.setSchoolLogo(filePath);
//                }
//
//                // Save updated school
//                School updatedSchool = schoolService.saveSchool(school);
//                return ResponseEntity.ok(new ResponseMessage<>("School updated successfully!", updatedSchool));
//            } else {
//                return ResponseEntity.status(404).body(new ResponseMessage<>("School not found!", null));
//            }
//        } catch (IOException e) {
//            return ResponseEntity.status(500).body(new ResponseMessage<>("Error updating school: " + e.getMessage(), null));
//        }
//    }

    @GetMapping("/sc/{schoolId}")
    public ResponseEntity<ResponseModel<School>> gets(@PathVariable String schoolId) {
        Optional<School> school = schoolService.getSchool(schoolId);

        if (school.isPresent()) {
            return ResponseEntity.ok(new ResponseModel<>("School found successfully!", school.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseModel<>("School not found with ID: " + schoolId, null));
        }
    }


    // Delete school by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<Void>> deleteSchool(@PathVariable String id) {
        boolean isDeleted = schoolService.deleteSchool(id);
        if (isDeleted) {
            return ResponseEntity.ok(new ResponseModel<>("School deleted successfully!", null));
        } else {
            return ResponseEntity.status(404).body(new ResponseModel<>("School not found!", null));
        }
    }

}

