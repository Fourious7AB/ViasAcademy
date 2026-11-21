package com.neel.StudentAdmission.service;


import com.neel.StudentAdmission.model.AuthUser;
import com.neel.StudentAdmission.model.SignUp;
import com.neel.StudentAdmission.repo.SignUpRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.JSONObject;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class SignUpService {
    @Value("${razorpay.keyId}")
    private String keyId;

    @Value("${razorpay.keySecret}")
    private String keySecret;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";
    private static final int PASSWORD_LENGTH = 8;
    @Autowired
    private SignUpRepository signUpRepository;

    public List<SignUp> getAllSignUps() {
        return signUpRepository.findAll();
    }

    public Optional<SignUp> getSignUpById(String id) {
        return signUpRepository.findById(id);
    }

    public SignUp createSignUp(SignUp signUp) {
        // Check for duplicate email and phone number
        if (signUpRepository.findByEmail(signUp.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists!");
        }
        if (signUpRepository.findByPhoneNumber(signUp.getPhoneNumber()).isPresent()) {
            throw new IllegalArgumentException("Phone number already exists!");
        }
        saveAuthUser(signUp.getEmail(),signUp.getPassword());
        return signUpRepository.save(signUp);
    }

    public SignUp updateSignUp(String id, SignUp updatedSignUp) {
        // Check if the ID exists
        SignUp existingSignUp = signUpRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SignUp not found!"));

        // Check for duplicate email and phone number (excluding the current record)
        signUpRepository.findByEmail(updatedSignUp.getEmail())
                .filter(signUp -> !signUp.getId().equals(id))
                .ifPresent(signUp -> {
                    throw new IllegalArgumentException("Email already exists!");
                });

        signUpRepository.findByPhoneNumber(updatedSignUp.getPhoneNumber())
                .filter(signUp -> !signUp.getId().equals(id))
                .ifPresent(signUp -> {
                    throw new IllegalArgumentException("Phone number already exists!");
                });

        // Update fields
        existingSignUp.setUserName(updatedSignUp.getUserName());
        existingSignUp.setEmail(updatedSignUp.getEmail());
        existingSignUp.setPhoneNumber(updatedSignUp.getPhoneNumber());
        existingSignUp.setSchoolName(updatedSignUp.getSchoolName());
        existingSignUp.setAddress(updatedSignUp.getAddress());
        existingSignUp.setInstituteType(updatedSignUp.getInstituteType());

        return signUpRepository.save(existingSignUp);
    }

    public void deleteSignUp(String id) {
        signUpRepository.deleteById(id);
    }
    private void saveAuthUser(String email,String password) {

        // Create and populate AuthUser object
        AuthUser authUser = new AuthUser();
        authUser.setUsername(email);
        authUser.setPassword(password);
        authUser.setRole("admin");

        // REST Call to AuthService
        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.postForEntity("https://school.vaisacademy.com/service4/api/auth/register", authUser, String.class);
        restTemplate.postForEntity("https://collage.vaisacademy.com/service4/api/auth/register", authUser, String.class);
//        restTemplate.postForEntity("http://localhost:8092/api/auth/register", authUser, String.class);

    }
    public boolean isPasswordStrong(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=]).{8,}$");
    }

    public Order createOrder(int amount) throws Exception {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        // Create an order request with amount, currency, and receipt information
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100);  // amount in paise (1 INR = 100 paise)
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "order_receipt_123");

        // Create the order on Razorpay and return the full order object
        return client.orders.create(orderRequest);
    }
    // Method to capture payment


    public String capturePayment(String paymentId) throws Exception {
        // Create Razorpay client instance
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        // Fetch the payment details using the payment ID
        Payment payment = client.payments.fetch(paymentId);

        // Get the amount to capture from the payment details
        long amount = payment.get("amount"); // Amount in paise (e.g., 50000 for ₹500.00)

        // Prepare the JSONObject for capture
        JSONObject captureRequest = new JSONObject();
        captureRequest.put("amount", amount); // Amount must match the payment's amount

        // Capture the payment
        Payment capturedPayment = client.payments.capture(paymentId, captureRequest);

        return "Payment captured successfully. Details: " + capturedPayment.toString();
    }

    // Method to verify the payment signature
    private boolean verifySignature(String paymentId, String signature, String orderId) {
        // Generate the expected signature using the orderId, paymentId, and the keySecret
        String data = orderId + "|" + paymentId;
        String expectedSignature = org.apache.commons.codec.digest.DigestUtils.sha256Hex(data + "|" + keySecret);

        // Compare the generated signature with the received signature
        return expectedSignature.equals(signature);
    }
    public Optional<SignUp> getLibrarianByEmail(String email) {
        return signUpRepository.findByEmail(email);
    }


}
