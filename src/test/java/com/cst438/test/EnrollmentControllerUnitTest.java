package com.cst438.test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.TermRepository;
import com.cst438.domain.UserRepository;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.test.utils.TestUtils;

/**
 * Unit tests for the enrollment functionality
 * Tests the POST /enrollments/sections/{sectionNo}?studentId={id} endpoint
 */
@AutoConfigureMockMvc
@SpringBootTest
public class EnrollmentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TermRepository termRepository;

    /**
     * Test 1: Successful Enrollment into a Section
     * A student successfully enrolls into a section.
     */
    @Test
    public void testSuccessfulEnrollment() throws Exception {
        // Use section 5 (CST438) which the student is not enrolled in yet
        int sectionNo = 5;
        int studentId = 3; // Thomas Edison

        // Get the section and its term
        Section section = sectionRepository.findById(sectionNo).orElse(null);
        assertNotNull(section);
        
        // Save the original add date and deadline
        Term term = section.getTerm();
        Date originalAddDate = term.getAddDate();
        Date originalAddDeadline = term.getAddDeadline();
        
        try {
            // Modify the term's add date and deadline to include today
            long currentTime = System.currentTimeMillis();
            Date yesterday = new Date(currentTime - 86400000); // Yesterday
            Date tomorrow = new Date(currentTime + 86400000); // Tomorrow
            
            term.setAddDate(yesterday);
            term.setAddDeadline(tomorrow);
            termRepository.save(term);
            
            // Verify the student is not already enrolled in this section
            Enrollment existingEnrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
            if (existingEnrollment != null) {
                // Clean up if there's an existing enrollment
                enrollmentRepository.delete(existingEnrollment);
            }

            // Make the POST request to enroll the student
            MockHttpServletResponse response = mvc.perform(
                    MockMvcRequestBuilders
                            .post("/enrollments/sections/" + sectionNo)
                            .param("studentId", String.valueOf(studentId))
                            .accept(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse();

            // Check that the response status is 200 (OK)
            assertEquals(200, response.getStatus());

            // Parse the response to get the EnrollmentDTO
            EnrollmentDTO result = TestUtils.fromJsonString(response.getContentAsString(), EnrollmentDTO.class);

            // Verify the enrollment data
            assertNotNull(result);
            assertEquals(studentId, result.studentId());
            assertEquals(sectionNo, result.sectionNo());
            assertNull(result.grade()); // New enrollment should have null grade

            // Verify the enrollment was added to the database
            Enrollment enrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
            assertNotNull(enrollment);
            assertEquals(studentId, enrollment.getStudent().getId());
            assertEquals(sectionNo, enrollment.getSection().getSectionNo());

            // Clean up - delete the test enrollment
            enrollmentRepository.delete(enrollment);
        } finally {
            // Restore the original add date and deadline
            term.setAddDate(originalAddDate);
            term.setAddDeadline(originalAddDeadline);
            termRepository.save(term);
        }
    }

    /**
     * Test 2: Duplicate Enrollment Attempt
     * A student attempts to enroll in a section but fails because the student is already enrolled.
     */
    @Test
    public void testDuplicateEnrollment() throws Exception {
        // Use section 1 (CST338) which the student is already enrolled in according to data.sql
        int sectionNo = 1;
        int studentId = 3; // Thomas Edison

        // Get the section and its term
        Section section = sectionRepository.findById(sectionNo).orElse(null);
        assertNotNull(section);
        
        // Save the original add date and deadline
        Term term = section.getTerm();
        Date originalAddDate = term.getAddDate();
        Date originalAddDeadline = term.getAddDeadline();
        
        try {
            // Modify the term's add date and deadline to include today
            long currentTime = System.currentTimeMillis();
            Date yesterday = new Date(currentTime - 86400000); // Yesterday
            Date tomorrow = new Date(currentTime + 86400000); // Tomorrow
            
            term.setAddDate(yesterday);
            term.setAddDeadline(tomorrow);
            termRepository.save(term);
            
            // Ensure the student is enrolled in this section
            Enrollment existingEnrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
            if (existingEnrollment == null) {
                // Create an enrollment if it doesn't exist
                existingEnrollment = new Enrollment();
                existingEnrollment.setStudent(userRepository.findById(studentId).orElse(null));
                existingEnrollment.setSection(section);
                enrollmentRepository.save(existingEnrollment);
            }
            
            // Make the POST request to try to enroll the student again
            MockHttpServletResponse response = mvc.perform(
                    MockMvcRequestBuilders
                            .post("/enrollments/sections/" + sectionNo)
                            .param("studentId", String.valueOf(studentId))
                            .accept(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse();

            // Check that the response status is 400 (Bad Request)
            assertEquals(400, response.getStatus());

            // Verify the error message
            assertEquals("student already enrolled", response.getErrorMessage());
        } finally {
            // Restore the original add date and deadline
            term.setAddDate(originalAddDate);
            term.setAddDeadline(originalAddDeadline);
            termRepository.save(term);
        }
    }

    /**
     * Test 3: Enrollment with an Invalid Section Number
     * A student attempts to enroll in a section using an invalid section number.
     */
    @Test
    public void testInvalidSectionNumber() throws Exception {
        // Use a section number that doesn't exist
        int invalidSectionNo = 999;
        int studentId = 3; // Thomas Edison

        // Make the POST request with an invalid section number
        MockHttpServletResponse response = mvc.perform(
                MockMvcRequestBuilders
                        .post("/enrollments/sections/" + invalidSectionNo)
                        .param("studentId", String.valueOf(studentId))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // Check that the response status is 404 (Not Found)
        assertEquals(404, response.getStatus());

        // Verify the error message
        assertEquals("section not found " + invalidSectionNo, response.getErrorMessage());
    }

    /**
     * Test 4: Enrollment Past the Add Deadline
     * A student attempts to enroll in a section, but the enrollment fails because the add deadline for that section has passed.
     */
    @Test
    public void testEnrollmentPastAddDeadline() throws Exception {
        // Use section 6 (CST338 in Spring 2025)
        int sectionNo = 6;
        int studentId = 3; // Thomas Edison

        // Get the section and its term
        Section section = sectionRepository.findById(sectionNo).orElse(null);
        assertNotNull(section);
        
        // Save the original add deadline
        Term term = section.getTerm();
        Date originalAddDeadline = term.getAddDeadline();
        Date originalAddDate = term.getAddDate();
        
        try {
            // Modify the term's add deadline to be in the past
            long currentTime = System.currentTimeMillis();
            Date pastDate = new Date(currentTime - 86400000); // Yesterday
            
            term.setAddDeadline(pastDate);
            term.setAddDate(new Date(currentTime - 172800000)); // Two days ago
            termRepository.save(term);
            
            // Verify the student is not already enrolled in this section
            Enrollment existingEnrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
            if (existingEnrollment != null) {
                // Clean up if there's an existing enrollment
                enrollmentRepository.delete(existingEnrollment);
            }
            
            // Make the POST request to try to enroll the student
            MockHttpServletResponse response = mvc.perform(
                    MockMvcRequestBuilders
                            .post("/enrollments/sections/" + sectionNo)
                            .param("studentId", String.valueOf(studentId))
                            .accept(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse();
            
            // Check that the response status is 400 (Bad Request)
            assertEquals(400, response.getStatus());
            
            // Verify the error message contains information about the add deadline
            assertTrue(response.getErrorMessage().contains("today not between"));
            
        } finally {
            // Restore the original add deadline
            term.setAddDeadline(originalAddDeadline);
            term.setAddDate(originalAddDate);
            termRepository.save(term);
        }
    }
}