package com.cst438.service;

import com.cst438.domain.Course;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Term;
import com.cst438.domain.TermRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.GradeDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.dto.UserDTO;
import com.cst438.dto.FinalGradeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class GradebookServiceProxy {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CourseRepository courseRepository;
    
    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    private TermRepository termRepository;

    @Value("registrar_service")
    private String registrarQueueName;

    @Bean
    public Queue createQueue() { return new Queue("gradebook_service", true); }

    @Bean
    public Queue registrarQueue() {
        return new Queue(registrarQueueName, true);
    }

    public void addCourse(CourseDTO dto) {
        sendMessage("addCourse " + asJsonString(dto));
    }

    public void updateCourse(CourseDTO dto) {
        sendMessage("updateCourse " + asJsonString(dto));
    }

    public void deleteCourse(String courseId) {
        sendMessage("deleteCourse " + courseId);
    }

    public void addSection(SectionDTO dto) {
        sendMessage("addSection " + asJsonString(dto));
    }

    public void updateSection(SectionDTO dto) {
        sendMessage("updateSection " + asJsonString(dto));
    }

    public void deleteSection(String sectionId) {
        sendMessage("deleteSection " + sectionId);
    }

    public void addUser(UserDTO dto) {
        sendMessage("addUser " + asJsonString(dto));
    }

    public void updateUser(UserDTO dto) {
        sendMessage("updateUser " + asJsonString(dto));
    }

    public void deleteUser(String userId) {
        sendMessage("deleteUser " + userId);
    }

    public void addEnrollment(EnrollmentDTO dto) {
        sendMessage("addEnrollment " + asJsonString(dto));
    }

    public void addAssignment(AssignmentDTO dto) {
        sendMessage("addAssignment " + asJsonString(dto));
    }

    public void updateAssignment(AssignmentDTO dto) {
        sendMessage("updateAssignment " + asJsonString(dto));
    }

    public void updateGrade(GradeDTO dto) {
        sendMessage("updateGrade " + asJsonString(dto));
    }

    public void deleteAssignment(String id) {
        sendMessage("deleteAssignment " + id);
    }

    // update enrollment
    public void updateEnrollment(EnrollmentDTO dto) {
        sendMessage("updateEnrollment " + asJsonString(dto));
    }

    public void deleteEnrollment(String enrollmentId) {
        sendMessage("deleteEnrollment " + enrollmentId);
    }

    public void sendFinalGrade(FinalGradeDTO dto) {
        sendMessage("updateFinalGrade " + asJsonString(dto));
    }



    @RabbitListener(queues = "gradebook_service")
    public void receive(String msg) {
        System.out.println("Receiving " + msg);
        try {
            String[] parts = msg.split(" ", 2);
            String action = parts[0];
            if (action.equals("addCourse")) {
                CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
                Course c = new Course();
                c.setCourseId(dto.courseId());
                c.setTitle(dto.title());
                c.setCredits(dto.credits());
                courseRepository.save(c);
            } else if (action.equals("updateCourse")) {
                CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
                Course c = courseRepository.findById(dto.courseId()).orElseThrow();
                c.setTitle(dto.title());
                c.setCredits(dto.credits());
                courseRepository.save(c);
            } else if (action.equals("deleteCourse")) {
                courseRepository.deleteById(parts[1]);
            } else if (action.equals("addSection")) {
                SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
                Section s = new Section();
                s.setSecId(dto.secId());
                s.setBuilding(dto.building());
                s.setRoom(dto.room());
                s.setTimes(dto.times());
                s.setInstructor_email(dto.instructorEmail());
                // Look up Course and Term
                Course course = courseRepository.findById(dto.courseId()).orElse(null);
                if (course != null) s.setCourse(course);
                Term term = termRepository.findByYearAndSemester(dto.year(), dto.semester());
                if (term != null) s.setTerm(term);
                sectionRepository.save(s);
            } else if (action.equals("updateSection")) {
                SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
                Section s = sectionRepository.findById(dto.secNo()).orElseThrow();
                s.setBuilding(dto.building());
                s.setRoom(dto.room());
                s.setTimes(dto.times());
                s.setInstructor_email(dto.instructorEmail());
                // Look up Course and Term
                Course course = courseRepository.findById(dto.courseId()).orElse(null);
                if (course != null) s.setCourse(course);
                Term term = termRepository.findByYearAndSemester(dto.year(), dto.semester());
                if (term != null) s.setTerm(term);
                sectionRepository.save(s);
            } else if (action.equals("deleteSection")) {
                sectionRepository.deleteById(Integer.parseInt(parts[1]));
            } else if (action.equals("addUser")) {
                UserDTO dto = fromJsonString(parts[1], UserDTO.class);
                User u = new User();
                u.setId(dto.id());
                u.setName(dto.name());
                u.setEmail(dto.email());
                u.setType(dto.type());
                userRepository.save(u);
            } else if (action.equals("updateUser")) {
                UserDTO dto = fromJsonString(parts[1], UserDTO.class);
                User u = userRepository.findById(dto.id()).orElseThrow();
                u.setName(dto.name());
                u.setEmail(dto.email());
                u.setType(dto.type());
                userRepository.save(u);
            } else if (action.equals("deleteUser")) {
                userRepository.deleteById(Integer.parseInt(parts[1]));
            } else if (action.equals("addEnrollment")) {
                EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
                Enrollment e = new Enrollment();
                e.setEnrollmentId(dto.enrollmentId());
                e.setGrade(dto.grade());
                // Look up User and Section
                User student = userRepository.findById(dto.studentId()).orElse(null);
                if (student != null) e.setStudent(student);
                Section section = sectionRepository.findById(dto.sectionNo()).orElse(null);
                if (section != null) e.setSection(section);
                enrollmentRepository.save(e);
            } else if (action.equals("updateEnrollment")) {
                EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
                Enrollment e = enrollmentRepository.findById(dto.enrollmentId()).orElseThrow();
                e.setGrade(dto.grade());
                // Look up User and Section
                User student = userRepository.findById(dto.studentId()).orElse(null);
                if (student != null) e.setStudent(student);
                Section section = sectionRepository.findById(dto.sectionNo()).orElse(null);
                if (section != null) e.setSection(section);
                enrollmentRepository.save(e);
            } else if (action.equals("deleteEnrollment")) {
                enrollmentRepository.deleteById(Integer.parseInt(parts[1]));
            } else if (action.equals("updateFinalGrade")) {
                FinalGradeDTO dto = fromJsonString(parts[1], FinalGradeDTO.class);
                Enrollment e = enrollmentRepository.findById(dto.enrollmentId()).orElseThrow();
                e.setGrade(dto.finalGrade());
                enrollmentRepository.save(e);
            }
        } catch (Exception ex) {
            // Swallow exception to prevent infinite redelivery
        }
    }

    public void sendMessage(String msg){
        System.out.println("Sending " + msg);
        try {
            rabbitTemplate.convertAndSend(registrarQueueName, msg);
        } catch (Exception e) {
            // Swallow exception to prevent poison message requeue
        }
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}