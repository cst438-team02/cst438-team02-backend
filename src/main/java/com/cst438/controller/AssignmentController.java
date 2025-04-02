package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.List;
import java.sql.Date;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    /**
     instructor lists assignments for a section.
     Assignment data is returned ordered by due date.
     logged in user must be the instructor for the section (assignment 7)
     */
    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(
            @PathVariable("secNo") int secNo) {
		
		// hint: use the assignment repository method 
		//  findBySectionNoOrderByDueDate to return 
		//  a list of assignments

        List<Assignment> assignments = assignmentRepository.findBySectionNoOrderByDueDate(secNo);
        List<AssignmentDTO> dtos = new ArrayList<>();

        for (Assignment a : assignments) {
            AssignmentDTO dto = new AssignmentDTO(
                    a.getAssignmentId(),
                    a.getTitle(),
                    a.getDueDate() != null ? a.getDueDate().toString() : null,
                    a.getSection().getCourse().getCourseId(),
                    a.getSection().getSecId(),
                    a.getSection().getSectionNo()
            );
            dtos.add(dto);
        }

        return dtos;
    }

    /**
     instructor creates an assignment for a section.
     Assignment data with primary key is returned.
     logged in user must be the instructor for the section (assignment 7)
     */
    @PostMapping("/assignments")
    public AssignmentDTO createAssignment(
            @RequestBody AssignmentDTO dto) {

        Optional<Section> sectionOpt = sectionRepository.findById(dto.secNo());
        if (sectionOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section not found");
        }

        Section section = sectionOpt.get();

        // check if due date for assignment is after the end date of the term
        Term term = section.getTerm(); // Get the term associated with the section
        // convert due date to due date
        Date dueDate = dto.dueDate() != null ? Date.valueOf(dto.dueDate()) : null;
        // check if the assignment due date is after the term's end date
        if (dueDate != null && dueDate.after(term.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment due date cannot be after the term end date.");
        }

        Assignment newAssignment = new Assignment();
        newAssignment.setTitle(dto.title());
        newAssignment.setDueDate(dto.dueDate() != null ? Date.valueOf(dto.dueDate()) : null);
        newAssignment.setSection(section);

        newAssignment = assignmentRepository.save(newAssignment);

        return new AssignmentDTO(
                newAssignment.getAssignmentId(),
                newAssignment.getTitle(),
                newAssignment.getDueDate() != null ? newAssignment.getDueDate().toString() : null,
                section.getCourse().getCourseId(),
                section.getSecId(),
                section.getSectionNo()
        );
    }

    /**
     instructor updates an assignment for a section.
     only title and dueDate may be changed
     updated assignment data is returned
     logged in user must be the instructor for the section (assignment 7)
     */
    @PutMapping("/assignments")
    public AssignmentDTO updateAssignment(@RequestBody AssignmentDTO dto) {

        Optional<Assignment> assignmentOpt = assignmentRepository.findById(dto.id());
        if (assignmentOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found");
        }

        Assignment assignment = assignmentOpt.get();
        assignment.setTitle(dto.title());
        assignment.setDueDate(dto.dueDate() != null ? Date.valueOf(dto.dueDate()) : null);

        assignment = assignmentRepository.save(assignment);

        Section section = assignment.getSection();
        return new AssignmentDTO(
                assignment.getAssignmentId(),
                assignment.getTitle(),
                assignment.getDueDate() != null ? assignment.getDueDate().toString() : null,
                section.getCourse().getCourseId(),
                section.getSecId(),
                section.getSectionNo()
        );
    }

    /**
     instructor deletes an assignment for a section.
     logged in user must be the instructor for the section (assignment 7)
     */
    @DeleteMapping("/assignments/{assignmentId}")
    public void deleteAssignment(@PathVariable("assignmentId") int assignmentId) {

        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found");
        }

        assignmentRepository.deleteById(assignmentId);
    }
}
