package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    /**
     students lists there enrollments given year and semester value
     returns list of enrollments, may be empty
     logged in user must be the student (assignment 7)
     */
   @GetMapping("/enrollments")
   public List<EnrollmentDTO> getSchedule(
           @RequestParam("year") int year,
           @RequestParam("semester") String semester,
           @RequestParam("studentId") int studentId) {


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
    public List<AssignmentStudentDTO> getStudentAssignments(
            @RequestParam("studentId") int studentId,
            @RequestParam("year") int year,
            @RequestParam("semester") String semester) {

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