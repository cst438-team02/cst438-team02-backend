package com.cst438.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Grade;
import com.cst438.domain.GradeRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.EnrollmentDTO;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     students lists there enrollments given year and semester value
     returns list of enrollments, may be empty
     logged in user must be the student (assignment 7)
     */
   @GetMapping("/enrollments")
   @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
   public List<EnrollmentDTO> getSchedule(
           @RequestParam("year") int year,
           @RequestParam("semester") String semester,
           Principal principal) {

    // Get student ID from principal
    String studentEmail = principal.getName();
    User student = userRepository.findByEmail(studentEmail);
    if (student == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
    }
    
    int studentId = student.getId();
    List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, studentId);

    List<EnrollmentDTO> result = new ArrayList<>();

    for (Enrollment e : enrollments) {
        EnrollmentDTO dto = new EnrollmentDTO(
            e.getEnrollmentId(),             
            e.getGrade(),                    
            e.getStudent().getId(),         
            e.getStudent().getName(),     
            e.getStudent().getEmail(),      
            e.getSection().getCourse().getCourseId(),   
            e.getSection().getCourse().getTitle(),    
            e.getSection().getSecId(),   
            e.getSection().getSectionNo(),  
            e.getSection().getBuilding(),    
            e.getSection().getRoom(),  
            e.getSection().getTimes(), 
            e.getSection().getCourse().getCredits(),
            e.getSection().getTerm().getYear(),  
            e.getSection().getTerm().getSemester()
        );
        result.add(dto);
    }
    return result;
   }

    /**
     students lists there assignments given year and semester value
     returns list of assignments may be empty
     logged in user must be the student (assignment 7)
     */
    @GetMapping("/assignments")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<AssignmentStudentDTO> getStudentAssignments(
            @RequestParam("year") int year,
            @RequestParam("semester") String semester,
            Principal principal) {

        // Get student ID from principal
        String studentEmail = principal.getName();
        User student = userRepository.findByEmail(studentEmail);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }
        
        int studentId = student.getId();
        List<Assignment> assignments = assignmentRepository.findByStudentIdAndYearAndSemesterOrderByDueDate(studentId, year, semester);
        List<AssignmentStudentDTO> assignmentDTOs = new ArrayList<>();

        for (Assignment assignment : assignments) {
            Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(
                    assignment.getSection().getSectionNo(), assignment.getAssignmentId());
            Integer score = (grade != null) ? grade.getScore() : null;

            assignmentDTOs.add(new AssignmentStudentDTO(
                    assignment.getAssignmentId(),
                    assignment.getTitle(),
                    assignment.getDueDate(), 
                    assignment.getSection().getCourse().getCourseId(),
                    assignment.getSection().getSecId(),
                    score
            ));
        }
        return assignmentDTOs;
    }

}