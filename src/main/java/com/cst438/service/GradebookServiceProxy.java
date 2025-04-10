package com.cst438.service;

import com.cst438.domain.Course;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Bean
    public Queue courseQueue() {
        return new Queue("registrar.course", true);
    }

    @Bean
    public Queue sectionQueue() {
        return new Queue("registrar.section", true);
    }

    @Bean
    public Queue userQueue() {
        return new Queue("registrar.user", true);
    }

    @Bean
    public Queue enrollmentQueue() {
        return new Queue("registrar.enrollment", true);
    }

    @Bean
    public Queue finalGradeQueue() {
        return new Queue("gradebook.finalgrade", true);
    }

    @RabbitListener(queues = "registrar.course")
    public void receiveCourse(String message) {
        try {
            Course c = fromJsonString(message, Course.class);
            courseRepository.save(c);
        } catch (Exception e) {
            System.err.println("Failed to process Course: " + e.getMessage());
        }
    }

    @RabbitListener(queues = "registrar.section")
    public void receiveSection(String message) {
        try {
            Section s = fromJsonString(message, Section.class);
            sectionRepository.save(s);
        } catch (Exception e) {
            System.err.println("Failed to process Section: " + e.getMessage());
        }
    }

    @RabbitListener(queues = "registrar.user")
    public void receiveUser(String message) {
        try {
            User u = fromJsonString(message, User.class);
            userRepository.save(u);
        } catch (Exception e) {
            System.err.println("Failed to process User: " + e.getMessage());
        }
    }

    @RabbitListener(queues = "registrar.enrollment")
    public void receiveEnrollment(String message) {
        try {
            Enrollment e = fromJsonString(message, Enrollment.class);
            enrollmentRepository.save(e);
        } catch (Exception e1) {
            System.err.println("Failed to process Enrollment: " + e1.getMessage());
        }
    }

    public void sendMessage(String action, Enrollment enrollment) {
        try {
            String message = action + " " + asJsonString(enrollment);
            rabbitTemplate.convertAndSend("gradebook.finalgrade", message);
        } catch (Exception e) {
            System.err.println("Error sending final grade: " + e.getMessage());
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