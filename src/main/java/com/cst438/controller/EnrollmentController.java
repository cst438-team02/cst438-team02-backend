package com.cst438.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.http.ResponseEntity;
import com.cst438.dto.ErrorResponse;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    /**
     instructor gets list of enrollments for a section
     list of enrollments returned is in order by student name
     logged in user must be the instructor for the section (assignment 7)
     */
    @GetMapping("/sections/{sectionNo}/enrollments")
    public ResponseEntity<?> getEnrollments(
        @PathVariable("sectionNo") int sectionNo ) {
        try {
            // using enrollment Repository find enrollment entities by Section number ordered by student name
            List<Enrollment> enrollments = enrollmentRepository.findBySectionSectionNoOrderByStudentName(sectionNo);
            List<EnrollmentDTO> dto_list = new ArrayList<>(); // to hold enrollment DTOs
            // for each enrollment found, create an enrollment DTO then add it to list of DTOs
            for (Enrollment e : enrollments) {
                dto_list.add(new EnrollmentDTO(
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
                ));
            }
            // Return result wrapped using ResponseEntity.ok() with HTTP status 200
            return ResponseEntity.ok(dto_list);
        } catch (Exception e) {
            // In case of error, respond with an ErrorResponse also wrapped in ResponseEntity.ok()
            return ResponseEntity.ok(new ErrorResponse("An error occurred: " + e.getMessage()));
        }
    }

    // instructor uploads enrollments with the final grades for the section
    // user must be instructor for the section
    /**
     instructor updates enrollment grades
     only the grade attribute of enrollment can be changed
     logged in user must be the instructor for the section (assignment 7)
     */
    @PutMapping("/enrollments")
    public void updateEnrollmentGrade(@RequestBody List<EnrollmentDTO> dlist) {
        // for each EnrollmentDTO in the list
        for (EnrollmentDTO eDTO : dlist) {
            //  find the Enrollment entity using enrollmentId
            Enrollment e = enrollmentRepository.findById(eDTO.enrollmentId()).orElse(null);
            // if Enrollment entity not found, return not found error
            if (e==null) {
                throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "enrollment not found "+eDTO.enrollmentId());
            }
            //  update the grade and save back to database
            else {
                e.setGrade(eDTO.grade());
                enrollmentRepository.save(e);
            }
        }
    }
}
