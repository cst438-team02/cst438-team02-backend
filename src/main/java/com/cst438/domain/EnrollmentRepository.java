package com.cst438.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends CrudRepository<Enrollment, Integer> {

    // TODO uncomment the following lines as needed

    @Query("select e from Enrollment e where e.section.sectionNo=:sectionNo order by e.student.name")
    List<Enrollment> findBySectionSectionNoOrderByStudentName(int sectionNo);

    @Query("select e from Enrollment e where e.student.id=:studentId order by e.section.term.termId")
    List<Enrollment> findEnrollmentsByStudentIdOrderByTermId(int studentId);

    @Query("select e from Enrollment e where e.section.term.year=:year and e.section.term.semester=:semester and e.student.id=:studentId order by e.section.course.courseId")
    List<Enrollment> findByYearAndSemesterOrderByCourseId(int year, String semester, int studentId);

    @Query("select e from Enrollment e where e.section.sectionNo=:sectionNo and e.student.id=:studentId")
    Enrollment findEnrollmentBySectionNoAndStudentId(int sectionNo, int studentId);

    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :studentId AND e.section.term.tyear = :year AND e.section.term.semester = :semester")
    List<Enrollment> findByStudentIdAndYearAndSemester(@Param("studentId") int studentId, @Param("year") int year, @Param("semester") String semester);
}
