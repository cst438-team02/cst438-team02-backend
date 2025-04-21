package com.cst438.controller;

import java.security.Principal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.EnrollmentDTO;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentScheduleController {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    UserRepository userRepository;

    /**
     students lists their transcript containing all enrollments
     returns list of enrollments in chronological order
     logged in user must be the student (assignment 7)
     example URL  /transcript?studentId=19803
     */
    @GetMapping("/transcripts")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getTranscript(Principal principal) {

        // Get student ID from principal
        String studentEmail = principal.getName();
        User student = userRepository.findByEmail(studentEmail);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }
        
        int studentId = student.getId();
        
        // list course_id, sec_id, title, credit, grade
        // use enrollment repository method findEnrollmentsByStudentIdOrderByTermId
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(studentId);
        List<EnrollmentDTO> dto_list = new ArrayList<>();
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
        return dto_list;
    }


    /**
     students enrolls into a section of a course
     returns the enrollment data including primary key
     logged in user must be the student (assignment 7)
     */
    @PostMapping("/enrollments/sections/{sectionNo}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public EnrollmentDTO addCourse(
            @PathVariable int sectionNo,
            Principal principal) {

        // Get student from principal
        String studentEmail = principal.getName();
        User student = userRepository.findByEmail(studentEmail);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }
        
        int studentId = student.getId();

        // check that the Section entity with primary key sectionNo exists
        Section s = sectionRepository.findById(sectionNo).orElse(null);
        if (s==null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "section not found "+sectionNo);
        }

        // check that today is between addDate and addDeadline for the section
        Date today = new Date(System.currentTimeMillis());
        // if not between addDate and addDeadline
        if (today.compareTo(s.getTerm().getAddDate()) < 0 || today.compareTo(s.getTerm().getAddDeadline()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "today not between: "+
                    s.getTerm().getAddDate()+" and "+s.getTerm().getAddDeadline());
        }

        // check that student is not already enrolled into this section
        Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
        if (e != null) {
            throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "student already enrolled");
        }

        // create a new enrollment entity and save.  The enrollment grade will
        // be NULL until instructor enters final grades for the course.
        Enrollment enrollment = new Enrollment();
        // set enrollment to the student (student entity already created above)
        enrollment.setStudent(student);
        enrollment.setSection(s);
        enrollmentRepository.save(enrollment);


        // return enrollment DTO
        return new EnrollmentDTO(
                enrollment.getEnrollmentId(),
                null, // new enrollment no grade yet
                enrollment.getStudent().getId(),
                enrollment.getStudent().getName(),
                enrollment.getStudent().getEmail(),
                enrollment.getSection().getCourse().getCourseId(),
                enrollment.getSection().getCourse().getTitle(),
                enrollment.getSection().getSecId(),
                enrollment.getSection().getSectionNo(),
                enrollment.getSection().getBuilding(),
                enrollment.getSection().getRoom(),
                enrollment.getSection().getTimes(),
                enrollment.getSection().getCourse().getCredits(),
                enrollment.getSection().getTerm().getYear(),
                enrollment.getSection().getTerm().getSemester()
        );
    }


    /**
     students drops an enrollment for a section
     logged in user must be the student (assignment 7)
     */
    @DeleteMapping("/enrollments/{enrollmentId}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public void dropCourse(
            @PathVariable("enrollmentId") int enrollmentId,
            Principal principal) {

        // Get student from principal
        String studentEmail = principal.getName();
        User student = userRepository.findByEmail(studentEmail);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }
        
        // get the enrollment
        Enrollment e = enrollmentRepository.findById(enrollmentId).orElse(null);
        if (e == null) {
            // if enrollment doesn't exist, do nothing
            return;
        }
        
        // Verify the enrollment belongs to the student
        if (e.getStudent().getId() != student.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Not authorized to drop this enrollment");
        }

        // check that today is not after the dropDeadline for section
        Date today = new Date(System.currentTimeMillis());
        if (today.compareTo(e.getSection().getTerm().getDropDeadline()) > 0) {
            throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "today is after drop deadline: "
                    +e.getSection().getTerm().getDropDeadline());
        }

        // delete enrollment
        enrollmentRepository.delete(e);
    }


}
