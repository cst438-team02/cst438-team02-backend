package com.cst438.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // AssignmentRepository and GradeRepository removed (Assignment/Grade deleted)

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
    // getStudentAssignments method removed (Assignment/Grade deleted)

}