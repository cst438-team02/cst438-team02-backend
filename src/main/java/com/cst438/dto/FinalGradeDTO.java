package com.cst438.dto;

/**
 * Data Transfer Object for final grade update.
 */
public record FinalGradeDTO(
    int enrollmentId,
    String finalGrade
) {}