package com.cst438.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Grade;
import com.cst438.domain.GradeRepository;
import com.cst438.dto.GradeDTO;

@RestController
public class GradeController {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    /**
     * Instructor lists the grades for an assignment for all enrolled students.
     * Returns the list of GradeDTO for each student in the assignment's section,
     * ordered by student name. If a Grade entity doesn't exist yet for that
     * (assignment, enrollment) pair, one is created with a null score.
     */
    @GetMapping("/assignments/{assignmentId}/grades")
    public List<GradeDTO> getAssignmentGrades(@PathVariable("assignmentId") int assignmentId) {

        // find assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Assignment not found."));

        // get the section number from assignment
        int sectionNo = assignment.getSection().getSectionNo();

        // find enrollments for this section
        List<Enrollment> enrollments = enrollmentRepository.findBySectionSectionNoOrderByStudentName(sectionNo);
        // we'll sort them in-memory by student's name
        enrollments.sort(Comparator.comparing(e -> e.getStudent().getName()));

        // build the result list
        List<GradeDTO> gradesList = new ArrayList<>();

        // for each enrollment, find or create a Grade entity
        for (Enrollment e : enrollments) {
            Grade g = gradeRepository.findByEnrollmentIdAndAssignmentId(e.getEnrollmentId(), assignmentId);
            if (g == null) {
                // create new grade with null score
                g = new Grade();
                g.setAssignment(assignment);
                g.setEnrollment(e);
                gradeRepository.save(g);
            }
            // build GradeDTO using the constructor
            GradeDTO gd = new GradeDTO(
                g.getGradeId(),
                e.getStudent().getName(),
                e.getStudent().getEmail(),
                assignment.getTitle(),
                assignment.getSection().getCourse().getCourseId(),
                sectionNo,
                g.getScore()
            );
            gradesList.add(gd);
        }

        return gradesList;
    }

    /**
     * Instructor updates one or more assignment grades. Only the score attribute
     * can be changed.
     */
    @PutMapping("/grades")
    public void updateGrades(@RequestBody List<GradeDTO> dlist) {
        for (GradeDTO gd : dlist) {
            // find the grade entity
            Grade grade = gradeRepository.findById(gd.gradeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grade not found for ID " + gd.gradeId()));
            // update the score
            grade.setScore(gd.score());
            gradeRepository.save(grade);
        }
    }

}
