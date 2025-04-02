package com.cst438.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Section;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.SectionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;
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
import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;

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

    @Autowired
    AssignmentRepository assignmentRepository;

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

    /**
     * Test 4: Instructor adds a new assignment successfully
     */
    @Test
    public void addAssignment() throws Exception {

        MockHttpServletResponse response;

        // create new assignmentDTO
        AssignmentDTO assignment = new AssignmentDTO(
            0,
            "New Assignment",
            "2025-05-10",
            "cst438",
            1,
            10
        );

        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert assignment to String data and set as request content
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")  // Endpoint for assignments
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment))  // Convert assignment object to JSON
                )
                .andReturn()
                .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        AssignmentDTO result = fromJsonString(response.getContentAsString(), AssignmentDTO.class);

        // primary key should have a non-zero value from the database
        assertNotEquals(0, result.id());
        // check other fields of the DTO for expected values
        assertEquals("New Assignment", result.title());
        assertEquals("2025-05-10", result.dueDate());
        assertEquals("cst438", result.courseId());

        // check the database
        Assignment a = assignmentRepository.findById(result.id()).orElse(null);
        assertNotNull(a);
        assertEquals("cst438", a.getSection().getCourse().getCourseId());

        // clean up after test. issue http DELETE request for section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/assignments/"+result.id()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        a = assignmentRepository.findById(result.id()).orElse(null);
        assertNull(a);  // section should not be found after delete
    }

    /**
     * Test 5: Instructor adds a new assignment with a due date past the end date of the class
     */
    @Test
    public void addAssignmentBadDueDate() throws Exception {

        MockHttpServletResponse response;

        // create new assignmentDTO with past due date
        AssignmentDTO assignment = new AssignmentDTO(
            0,
            "Late Assignment",
            "2026-01-01",
            "cst438",
            1,
            10
        );

        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert assignment to String data and set as request content
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")  // Endpoint for assignments
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment))  // Convert assignment object to JSON
                )
                .andReturn()
                .getResponse();

        // expecting a 400 Bad Request due to invalid due date
        assertEquals(400, response.getStatus());

        // check that no assignment was created in the database
        boolean assignmentExists = false;
        for (Assignment a : assignmentRepository.findAll()) {  // Iterate over Iterable
            if (a.getTitle().equals("Late Assignment")) {
                assignmentExists = true;
                break;
            }
        }
        assertFalse(assignmentExists); // No assignment should exist

    }

    /**
     * Test 6: Instructor adds a new assignment with invalid section number.
     */
    @Test
    public void addAssignmentBadSecNo() throws Exception {

        MockHttpServletResponse response;

        // create new assignmentDTO with invalid section number
        AssignmentDTO assignment = new AssignmentDTO(
                0,
                "Assignment Invalid SecNo",
                "2026-01-01",
                "cst438",
                1,
                99
        );

        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert assignment to String data and set as request content
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")  // Endpoint for assignments
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment))  // Convert assignment object to JSON
                )
                .andReturn()
                .getResponse();

        // expecting a 400 Bad Request due to invalid due date
        assertEquals(400, response.getStatus());

        // check that no assignment was created in the database
        boolean assignmentExists = false;
        for (Assignment a : assignmentRepository.findAll()) {  // Iterate over Iterable
            if (a.getTitle().equals("Assignment Invalid SecNo")) {
                assignmentExists = true;
                break;
            }
        }
        assertFalse(assignmentExists); // No assignment should exist

    }


    /**
     * Utility methods
     */
    // Converts a Java object into a JSON string.
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // Converts a JSON string into a Java object of the specified type.
    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}