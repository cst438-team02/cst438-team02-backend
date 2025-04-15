package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.service.GradebookServiceProxy;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    GradebookServiceProxy gradebookServiceProxy;

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

        // Send updateAssignment to registrar service
        gradebookServiceProxy.updateAssignment(new AssignmentDTO(
                assignment.getAssignmentId(),
                assignment.getTitle(),
                assignment.getDueDate() != null ? assignment.getDueDate().toString() : null,
                section.getCourse().getCourseId(),
                section.getSecId(),
                section.getSectionNo()
        ));
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


    // ==== Moved from SectionController ====
    // get Sections for an instructor
    // example URL  /sections?instructorEmail=dwisneski@csumb.edu&year=2024&semester=Spring
    @GetMapping("/sections")
    public List<SectionDTO> getSectionsForInstructor(
            @RequestParam("email") String instructorEmail,
            @RequestParam("year") int year ,
            @RequestParam("semester") String semester )  {

        System.out.println("Fetching Sections");
        List<Section> sections = sectionRepository.findByInstructorEmailAndYearAndSemester(instructorEmail, year, semester);

        List<SectionDTO> dto_list = new ArrayList<>();
        for (Section s : sections) {
            User instructor = null;
            if (s.getInstructorEmail()!=null) {
                instructor = userRepository.findByEmail(s.getInstructorEmail());
            }
            dto_list.add(new SectionDTO(
                    s.getSectionNo(),
                    s.getTerm().getYear(),
                    s.getTerm().getSemester(),
                    s.getCourse().getCourseId(),
                    s.getCourse().getTitle(),
                    s.getSecId(),
                    s.getBuilding(),
                    s.getRoom(),
                    s.getTimes(),
                    (instructor!=null) ? instructor.getName() : "",
                    (instructor!=null) ? instructor.getEmail() : ""
            ));
        }
        return dto_list;
    }

}
