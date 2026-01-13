package com.tcs.allocation.dto;

public class CandidateDto {
    private EmployeeResponseDto employee;
    private double score;

    public EmployeeResponseDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeResponseDto employee) {
        this.employee = employee;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
