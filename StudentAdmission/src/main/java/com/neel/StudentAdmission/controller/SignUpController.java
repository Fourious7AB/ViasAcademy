package com.neel.StudentAdmission.controller;


import ch.qos.logback.core.net.server.Client;
import com.neel.StudentAdmission.model.ResponseMessage;
import com.neel.StudentAdmission.model.ResponseModel;
import com.neel.StudentAdmission.model.SignUp;
import com.neel.StudentAdmission.service.SignUpService;

import com.razorpay.Order;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/signups")
public class SignUpController {
    @Autowired
    private SignUpService signUpService;

    @GetMapping
    public ResponseEntity<ResponseMessage> getAllSignUps() {
        List<SignUp> signUps = signUpService.getAllSignUps();
        return ResponseEntity.ok(new ResponseMessage("Sign-ups retrieved successfully", signUps));
    }
    // Get Teacher by Email
    @GetMapping("/email/{email}")
    public ResponseMessage getTeacherByEmail(@PathVariable String email) {
        Optional<SignUp> librarian = signUpService.getLibrarianByEmail(email);
        return librarian.map(value -> new ResponseMessage("School Details retrieved successfully", value))
                .orElseGet(() -> new ResponseMessage("School Details not found with email: " + email, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage> getSignUpById(@PathVariable String id) {
        return signUpService.getSignUpById(id)
                .map(signUp -> ResponseEntity.ok(new ResponseMessage("Sign-up retrieved successfully", signUp)))
                .orElse(ResponseEntity.status(404).body(new ResponseMessage("Sign-up not found", null)));
    }

    @PostMapping
    public ResponseEntity<ResponseMessage> createSignUp(@RequestBody SignUp signUp) {
        try {
            SignUp createdSignUp = signUpService.createSignUp(signUp);
            return ResponseEntity.ok(new ResponseMessage("Sign-up created successfully", createdSignUp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseMessage> updateSignUp(@PathVariable String id, @RequestBody SignUp signUp) {
        try {
            SignUp updatedSignUp = signUpService.updateSignUp(id, signUp);
            return ResponseEntity.ok(new ResponseMessage("Sign-up updated successfully", updatedSignUp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage> deleteSignUp(@PathVariable String id) {
        signUpService.deleteSignUp(id);
        return ResponseEntity.ok(new ResponseMessage("Sign-up deleted successfully", null));
    }
    // Endpoint to create an order
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Integer> paymentDetails) {
        try {
            // Validate input
            if (!paymentDetails.containsKey("amount") || paymentDetails.get("amount") <= 0) {
                return ResponseEntity.badRequest().body("Invalid amount provided.");
            }

            // Extract payment amount
            int amount = paymentDetails.get("amount");

            // Call the service to create an order
            Order order = signUpService.createOrder(amount);

            // Return the created order object
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating order: " + e.getMessage());
        }
    }

    // Endpoint to capture payment
    @PostMapping("/capture")
    public ResponseEntity<String> capturePayment(@RequestParam String paymentId) {
        try {
            // Validate payment ID
            if (paymentId == null || paymentId.isEmpty()) {
                return ResponseEntity.badRequest().body("Payment ID is required.");
            }

            // Call the service to capture the payment
            String response = signUpService.capturePayment(paymentId);

            // Return success response
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error capturing payment: " + e.getMessage());
        }
    }
}
