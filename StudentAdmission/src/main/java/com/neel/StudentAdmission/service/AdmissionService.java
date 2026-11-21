package com.neel.StudentAdmission.service;

import com.neel.StudentAdmission.model.Admission;
import com.neel.StudentAdmission.model.AuthUser;
import com.neel.StudentAdmission.model.School;
import com.neel.StudentAdmission.repo.AdmissionRepository;
import com.neel.StudentAdmission.repo.SchoolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdmissionService {
    @Autowired
    private AdmissionRepository admissionRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    private static final String TEMPLATE_PATH = "https://res.cloudinary.com/djz6jcjhk/image/upload/v1740642340/schoolidmain_mewapb.png";
    private static final String OUTPUT_DIR = "src/main/resources/output/";
    private static final String CLOUDINARY_URL = "https://api.cloudinary.com/v1_1/djz6jcjhk/image/upload";
    private static final String API_KEY = "495141318574428";
    private static final String API_SECRET = "WxXxl8AaFXdeuHSfYmg4QrKH4O8";

    // Method to count total students
    public long getTotalStudentCount() {
        return admissionRepository.count();
    }
    
    // Method to count students for a specific school
    public long getStudentCountBySchool(String schoolId) {
        return admissionRepository.findBySchoolId(schoolId).size();
    }
    
    // Generate Admission Number
    private String generateAdmissionNumber(String schoolCode) {
        String year = String.valueOf(new java.util.Date().getYear() + 1900); // Current year
        long count = admissionRepository.count() + 1;
        return schoolCode + "" + year + "" + String.format("%03d", count);
    }
    
    // Save file to Cloudinary
    public String saveFile(MultipartFile file) throws IOException {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Prepare the request body (Multipart data)
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            body.add("upload_preset", "Teacher_image");
            body.add(API_KEY, API_SECRET);// Add the image file to the request
            // Optional preset

            // Prepare the headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create HttpEntity with the body and headers
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Make the HTTP POST request to Cloudinary
            ResponseEntity<Map> responseEntity = restTemplate.exchange(CLOUDINARY_URL, HttpMethod.POST, requestEntity, Map.class);

            // Extract and return the URL of the uploaded image from Cloudinary response
            Map<String, Object> responseBody = responseEntity.getBody();
            if (responseBody != null) {
                return (String) responseBody.get("secure_url"); // Return secure URL
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if upload fails
    }

//    // Create Admission with schoolId
//    public Admission createAdmission(Admission admission, String schoolId) {
//        System.out.println("School id"+schoolId);
//        Optional<School> school = schoolRepository.findById(schoolId);
//        if (school.isPresent()) {
//            String admissionNumber = generateAdmissionNumber(school.get().getSchoolCode());
//            admission.setAdmissionNumber(admissionNumber);
//            admission.setSchoolId(schoolId);
//
//            // Automatically create the authentication user
//            saveAuthUser(admission.getAdmissionNumber(), admission.getDob());
//            return admissionRepository.save(admission);
//        }
//        throw new RuntimeException("School not found");
//    }
// Create Admission
public Admission createAdmission(Admission admission,String schoolId) {
    Optional<School> school = schoolRepository.findBySchoolId(schoolId);
    if (school.isPresent()) {
        String admissionNumber = generateAdmissionNumber(school.get().getSchoolCode());
        admission.setAdmissionNumber(admissionNumber);
        admission.setSchoolId(schoolId);
//            // Generate ID Card after saving the admission
//            String outputPath = OUTPUT_DIR + "id_card_" + admissionNumber + ".png";
//            generateIDCardForAdmission(admission, outputPath);
        // Automatically create the authentication user
        saveAuthUser(admission.getAdmissionNumber(), admission.getDob());
        return admissionRepository.save(admission);
    }
    throw new RuntimeException("School not found");


}

    // Get all Admissions
    public Iterable<Admission> getAllAdmissions() {
        return admissionRepository.findAll();
    }
    
    // Get Admissions by schoolId
    public List<Admission> getAdmissionsBySchoolId(String schoolId) {
        return admissionRepository.findBySchoolId(schoolId);
    }

    // Get Admission by Admission Number
    public Optional<Admission> getAdmissionByNumber(String admissionNumber) {
        return admissionRepository.findByAdmissionNumber(admissionNumber);
    }
    
    // Get Admission by Admission Number and schoolId
    public Optional<Admission> getAdmissionByNumberAndSchool(String admissionNumber, String schoolId) {
        return admissionRepository.findByAdmissionNumberAndSchoolId(admissionNumber, schoolId);
    }

    // Update Admission by Admission Number
    public Admission updateAdmissionByNumber(String admissionNumber, Admission updatedAdmission) {
        Optional<Admission> admission = admissionRepository.findByAdmissionNumber(admissionNumber);
        if (admission.isPresent()) {
            updatedAdmission.setId(admission.get().getId()); // Retain the original ID
            updatedAdmission.setAdmissionNumber(admissionNumber); // Ensure the admission number remains the same
            return admissionRepository.save(updatedAdmission);
        }
        throw new RuntimeException("Admission not found with admission number: " + admissionNumber);
    }
    
    // Update Admission by Admission Number and schoolId
    public Admission updateAdmissionByNumberAndSchool(String admissionNumber, String schoolId, Admission updatedAdmission) {
        Optional<Admission> admission = admissionRepository.findByAdmissionNumberAndSchoolId(admissionNumber, schoolId);
        if (admission.isPresent()) {
            updatedAdmission.setId(admission.get().getId()); // Retain the original ID
            updatedAdmission.setAdmissionNumber(admissionNumber); // Ensure the admission number remains the same
            updatedAdmission.setSchoolId(schoolId); // Ensure the school ID remains the same
            return admissionRepository.save(updatedAdmission);
        }
        throw new RuntimeException("Admission not found with admission number: " + admissionNumber + " for this school");
    }

    // Delete Admission by Admission Number
    public void deleteAdmissionByNumber(String admissionNumber) {
        Optional<Admission> admission = admissionRepository.findByAdmissionNumber(admissionNumber);
        if (admission.isPresent()) {
            admissionRepository.delete(admission.get());
        } else {
            throw new RuntimeException("Admission not found with admission number: " + admissionNumber);
        }
    }
    
    // Delete Admission by Admission Number and schoolId
    public boolean deleteAdmissionByNumberAndSchool(String admissionNumber, String schoolId) {
        Optional<Admission> admission = admissionRepository.findByAdmissionNumberAndSchoolId(admissionNumber, schoolId);
        if (admission.isPresent()) {
            admissionRepository.delete(admission.get());
            return true;
        }
        return false;
    }
    
    // Generate and save ID card
    public String generateAndSaveIDCard(String admissionNumber) {
        Admission admission = admissionRepository.findByAdmissionNumber(admissionNumber)
                .orElseThrow(() -> new RuntimeException("Admission not found for number: " + admissionNumber));
        // Fetch school details using the schoolCode from admission
        School school = schoolRepository.findById("67ca9167d56ec91fc9539234")
                .orElseThrow(() -> new RuntimeException("School not found for ID: 67ca9167d56ec91fc9539234"));
        // Generate the ID card and save it to a file
        String outputPath = OUTPUT_DIR + "id_card_" + admissionNumber + ".png";
        generateIDCardForAdmission(admission, school, outputPath);

        // Save the output path in the admission record
        admission.setIdCardPath(outputPath);
        admissionRepository.save(admission);

        return outputPath;
    }
    
    // Generate and save ID card with schoolId validation
    public String generateAndSaveIDCardWithSchool(String admissionNumber, String schoolId) {
        try {
            Admission admission = admissionRepository.findByAdmissionNumberAndSchoolId(admissionNumber, schoolId)
                    .orElseThrow(() -> new RuntimeException("Admission not found for number: " + admissionNumber + " in this school"));
            
            // Verify student image exists
            if (admission.getStudentImage() == null || admission.getStudentImage().isEmpty()) {
                throw new RuntimeException("Student image is missing for admission: " + admissionNumber);
            }
            
            // Check if the image is in AVIF format and convert if needed
            String studentImagePath = admission.getStudentImage();
            if (studentImagePath.toLowerCase().endsWith(".avif")) {
                // Convert AVIF to JPG by modifying the Cloudinary URL
                String convertedUrl = studentImagePath.replace("/upload/", "/upload/f_jpg,q_auto/");
                System.out.println("Converting AVIF image to JPG format: " + convertedUrl);
                admission.setStudentImage(convertedUrl);
            }
            
            // Fetch school details using the schoolId
            School school = schoolRepository.findBySchoolId(schoolId)
                    .orElseThrow(() -> new RuntimeException("School not found for ID: " + schoolId));
            
            // Verify school logo exists
            if (school.getSchoolLogo() == null || school.getSchoolLogo().isEmpty()) {
                System.out.println("Warning: School logo is missing for school: " + schoolId);
            }
                    
            // Create output directory if it doesn't exist
            File outputDir = new File(OUTPUT_DIR);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // Generate the ID card and save it to a file
            String outputPath = OUTPUT_DIR + "id_card_" + admissionNumber + "_" + schoolId + ".png";
            System.out.println("Generating ID card at path: " + outputPath);
            
            generateIDCardForAdmission(admission, school, outputPath);
    
            // Save the output path in the admission record
            admission.setIdCardPath(outputPath);
            admissionRepository.save(admission);
    
            return outputPath;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating ID card: " + e.getMessage(), e);
        }
    }
    
    private void generateIDCardForAdmission(Admission admission, School school, String outputPath) {
        IDCardGenerator.generateIDCard(TEMPLATE_PATH, outputPath, admission, school);
    }

    // Create authentication user
    private void saveAuthUser(String admissionNumber, String dob) {
        String formattedDob = LocalDate.parse(dob).format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        AuthUser authUser = new AuthUser();
        authUser.setUsername(admissionNumber);
        authUser.setPassword(formattedDob);
        authUser.setRole("student");

        // REST Call to AuthService
        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.postForEntity("https://school.vaisacademy.com/service4/api/auth/register", authUser, String.class);
        restTemplate.postForEntity("https://collage.vaisacademy.com/service4/api/auth/register", authUser, String.class);
//        restTemplate.postForEntity("http://localhost:8092/api/auth/register", authUser, String.class);
    }
    
    // Update admission status with schoolId validation
    public Admission updateAdmissionStatus(String admissionNumber, String schoolId, String status) {
        Optional<Admission> admissionOpt = admissionRepository.findByAdmissionNumberAndSchoolId(admissionNumber, schoolId);
        
        if (admissionOpt.isPresent()) {
            Admission admission = admissionOpt.get();
            admission.setStatus(status);
            return admissionRepository.save(admission);
        }
        
        throw new RuntimeException("Admission not found with admission number: " + admissionNumber + " for this school");
    }
}


