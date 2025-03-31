package com.cst438.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.cst438.domain.Grade;
import com.cst438.domain.GradeRepository;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.TermRepository;
import com.cst438.domain.UserRepository;

@AutoConfigureMockMvc
@SpringBootTest
public class InstructorUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TermRepository termRepository;

    /**
     * Test 1: Instructor grades an assignment and enters scores for all enrolled students and uploads the scores
     * Instructor should be able to enter a score for an assigment for all students
     */
    @Test
    public void testUpdateAssignmentGrades() throws Exception {
        int gradeId = 1;
        double newScore = 88.0;

        String json = """
            [
                {
                    "gradeId": %d,
                    "studentName": "Test Student",
                    "studentEmail": "student@test.com",
                    "assignmentTitle": "Homework 1",
                    "courseId": "CST438",
                    "sectionNo": 1,
                    "score": %.1f
                }
            ]
            """.formatted(gradeId, newScore);

        MockHttpServletResponse response = mvc.perform(
                MockMvcRequestBuilders.put("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        Grade updated = gradeRepository.findById(gradeId).orElse(null);
        assertNotNull(updated);
        assertEquals(newScore, updated.getScore(), 0.001);
    }

    /**
     * Test 2: Instructor attempts to grade an assignment, but the assignment id is invalid
     * Intructor should attempt to grade the assignment, but should be an error due to invalid id
     */
    @Test
    public void testInvalidAssignmentId() throws Exception {
        int invalidAssignmentId = 9999;

        MockHttpServletResponse response = mvc.perform(
                MockMvcRequestBuilders.get("/assignments/" + invalidAssignmentId + "/grades")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertEquals(400, response.getStatus());
        assertTrue(response.getErrorMessage().contains("Assignment not found"));
    }

    /**
     * Test 3: Instructor enters final class grades for all enrolled students
     * Instructor should be able to enter grades for enrolled students
     */
    @Test
    public void testUploadFinalClassGrades() throws Exception {
        int enrollmentId = 1;
        String finalGrade = "B+";

        String json = """
            [
                {
                    "enrollmentId": %d,
                    "grade": "%s",
                    "studentId": 3,
                    "studentName": "Test Student",
                    "studentEmail": "student@test.com",
                    "courseId": "CST438",
                    "courseTitle": "Software Engineering",
                    "secId": "001",
                    "sectionNo": 1,
                    "building": "Main",
                    "room": "101",
                    "times": "MWF 2-3",
                    "credits": 3,
                    "year": 2025,
                    "semester": "Spring"
                }
            ]
            """.formatted(enrollmentId, finalGrade);

        MockHttpServletResponse response = mvc.perform(
                MockMvcRequestBuilders.put("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        Enrollment updated = enrollmentRepository.findById(enrollmentId).orElse(null);
        assertNotNull(updated);
        assertEquals(finalGrade, updated.getGrade());
    }

}