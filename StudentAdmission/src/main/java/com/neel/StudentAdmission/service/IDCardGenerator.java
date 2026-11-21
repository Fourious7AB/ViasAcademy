package com.neel.StudentAdmission.service;

import com.neel.StudentAdmission.model.Admission;
import com.neel.StudentAdmission.model.School;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import javax.imageio.ImageIO;

public class IDCardGenerator {

    public static void generateIDCard(String templatePath, String outputPath, Admission admission, School school) {
        try {
            // Load the template image from URL
            BufferedImage template;
            System.out.println("Loading template from: " + templatePath);
            URL templateUrl = new URL(templatePath);
            template = ImageIO.read(templateUrl);
            if (template == null) {
                throw new IOException("Failed to load template image from URL: " + templatePath);
            }

            Graphics2D graphics = template.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw School Name with Wrapping if Too Long
            graphics.setFont(new Font("Poppins", Font.BOLD, 36));
            graphics.setColor(Color.BLACK);
            String schoolName = school.getSchoolName();
            FontMetrics fm = graphics.getFontMetrics();
            int schoolNameWidth = fm.stringWidth(schoolName);
            int maxWidth = template.getWidth() - 100; // Leave padding on both sides

            if (schoolNameWidth > maxWidth) {
                // Scale Font to Fit
                int scaledFontSize = (int) (36.0 * maxWidth / schoolNameWidth);
                graphics.setFont(new Font("Poppins", Font.BOLD, scaledFontSize));
                fm = graphics.getFontMetrics();
                schoolNameWidth = fm.stringWidth(schoolName);
            }

            int schoolNameX = (template.getWidth() - schoolNameWidth) / 2;
            graphics.drawString(schoolName, schoolNameX, 50);

            // Draw dynamic text fields with different fonts and sizes
            // Admission No
            graphics.setFont(new Font("Poppins", Font.BOLD, 26));
            graphics.setColor(Color.BLACK);
            graphics.drawString("Admission No: " + admission.getAdmissionNumber(), 193, 762);

            // Name (centered)
            graphics.setFont(new Font("Poppins", Font.BOLD, 50));
            graphics.setColor(Color.BLUE);
            String studentName = admission.getStudentName();
            FontMetrics fontMetrics = graphics.getFontMetrics();
            int nameWidth = fontMetrics.stringWidth(studentName);
            int centerX = (template.getWidth() - nameWidth) / 2; // Center the text horizontally
            graphics.drawString(studentName, centerX, 479);

            // Class
            graphics.setFont(new Font("Poppins", Font.BOLD, 26));
            graphics.setColor(Color.BLACK);// Example: Helvetica, 28px
            graphics.drawString("Class: " + admission.getClassName(), 192, 646);

            // Section
            graphics.setFont(new Font("Times New Roman", Font.BOLD, 26));
            graphics.setColor(Color.BLACK);// Example: Times New Roman, 24px
            graphics.drawString("Section: " + admission.getSection(), 193, 705);

            // Blood Group
            graphics.setFont(new Font("Poppins", Font.BOLD, 26));
            graphics.setColor(Color.BLACK);// Example: Verdana, 26px
            graphics.drawString("Blood Group: " + admission.getBloodGroup(), 193, 820);

            // ✅ 5. Load & Draw School Logo
            BufferedImage schoolLogo = null;
            try (InputStream inputStream = new URL(school.getSchoolLogo()).openStream()) {
                schoolLogo = ImageIO.read(inputStream);
            }
            if (schoolLogo != null) {
                int logoX = 50;  // Adjust position as needed
                int logoY = 20;  // Adjust position as needed
                int logoWidth = 120;
                int logoHeight = 120;
                graphics.drawImage(schoolLogo, logoX, logoY, logoWidth, logoHeight, null);
            }

// Ensure the parent directories exist
            // Load the student image
//            String studentImagePath = admission.getStudentImage(); // Get the student image path
//            File studentImageFile = new File(studentImagePath);
//            BufferedImage studentImage = ImageIO.read(studentImageFile);
//            URL url = new URL(studentImagePath);
//            BufferedImage studentImage = ImageIO.read(url); // Read from URL
//            if (studentImage == null) {
//                throw new IOException("Unable to read the student image from the provided URL.");
//            }
            // Load the student image with improved error handling and AVIF support
            BufferedImage studentImage = null;
            String studentImagePath = admission.getStudentImage();
            System.out.println("Loading student image from: " + studentImagePath);
            
            try {
                if (studentImagePath == null || studentImagePath.isEmpty()) {
                    throw new IOException("Student image path is empty or null");
                }
            
                if (studentImagePath.startsWith("http") || studentImagePath.startsWith("https")) {
                    // Handle URL-based images
                    URL imageUrl = new URL(studentImagePath);
                    
                    // Check if it's an AVIF image and convert if needed
                    if (studentImagePath.toLowerCase().endsWith(".avif")) {
                        // For AVIF images, request a converted format from Cloudinary
                        String convertedUrl = convertAvifImageUrl(studentImagePath);
                        imageUrl = new URL(convertedUrl);
                        System.out.println("Converting AVIF image to JPG: " + convertedUrl);
                    }
                    
                    try (InputStream inputStream = imageUrl.openStream()) {
                        studentImage = ImageIO.read(inputStream);
                    }
                } else {
                    // Handle file-based images
                    File studentImageFile = new File(studentImagePath);
                    if (studentImageFile.exists()) {
                        studentImage = ImageIO.read(studentImageFile);
                    } else {
                        throw new IOException("Student image file does not exist: " + studentImagePath);
                    }
                }
            
                if (studentImage == null) {
                    throw new IOException("Failed to read student image from: " + studentImagePath);
                }
            } catch (Exception e) {
                System.out.println("Error loading student image: " + e.getMessage());
                // Create a placeholder image instead of failing
                studentImage = new BufferedImage(308, 308, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = studentImage.createGraphics();
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(0, 0, 308, 308);
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                g.drawString("No Image", 100, 150);
                g.dispose();
                System.out.println("Using placeholder image instead");
            }

            // Specify the size and position for the student image on the ID card
            int imageX = 172;
            int imageY = 104;
            int imageDiameter = 308;

            // Create a circular clipping region for the image
            graphics.setClip(new Ellipse2D.Double(imageX, imageY, imageDiameter, imageDiameter));

            // Draw the student image on the ID card (scaled to fit inside the circle)
            graphics.drawImage(studentImage, imageX, imageY, imageDiameter, imageDiameter, null);

            // Reset clipping to draw other elements correctly
            graphics.setClip(null);

            // Ensure output directory exists
            File outputFile = new File(outputPath);
            if (!outputFile.getParentFile().exists()) {
                System.out.println("Creating output directory: " + outputFile.getParentFile().getAbsolutePath());
                outputFile.getParentFile().mkdirs();
            }

            // Save the generated ID card image
            graphics.dispose();
            ImageIO.write(template, "png", outputFile);

            System.out.println("ID Card generated successfully at: " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating ID card: " + e.getMessage(), e);
        }
    }


// Add this helper method to convert AVIF URLs to JPG format
private static String convertAvifImageUrl(String avifUrl) {
    // If it's a Cloudinary URL, modify it to request JPG format
    if (avifUrl.contains("cloudinary.com")) {
        // Replace the file extension or add format transformation
        if (avifUrl.contains("/upload/")) {
            return avifUrl.replace("/upload/", "/upload/f_jpg,q_auto/");
        }
    }
    return avifUrl;
}
}
