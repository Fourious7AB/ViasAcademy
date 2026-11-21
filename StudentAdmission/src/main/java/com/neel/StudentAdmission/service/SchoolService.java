package com.neel.StudentAdmission.service;

import com.neel.StudentAdmission.model.School;
import com.neel.StudentAdmission.repo.SchoolRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SchoolService {
    private final SchoolRepository schoolRepository;

    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }
    private static final String CLOUDINARY_URL = "https://api.cloudinary.com/v1_1/djz6jcjhk/image/upload";
    private static final String API_KEY = "495141318574428";
    private static final String API_SECRET = "WxXxl8AaFXdeuHSfYmg4QrKH4O8";
//    @Value("${file.upload-dir}")
//    private String uploadDir;

    public String saveFile(MultipartFile file) throws IOException {

//        File directory = new File(uploadDir);
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//
//        // Save file with a unique name
//        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
//        File savedFile = new File(uploadDir + uniqueFileName);
//        file.transferTo(savedFile);
//
//        return savedFile.getPath();
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

    public School saveSchool(School school) {
        return schoolRepository.save(school);
    }


    public School getSchoolById(String id) {
        Optional<School> school = schoolRepository.findById(id);
        return school.orElse(null);
    }

    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    public boolean deleteSchool(String id) {
        Optional<School> school = schoolRepository.findById(id);
        if (school.isPresent()) {
            schoolRepository.delete(school.get());
            return true;
        }
        return false;
    }
    public Optional<School> getSchool(String schoolId) {
        return schoolRepository.findBySchoolId(schoolId);
    }

}

