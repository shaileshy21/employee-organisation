package com.organisation.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Employee {
    private Integer id;
    private String firstName;
    private String lastName;
    private BigDecimal salary;
    private Integer managerId;
    @Builder.Default
    private List<Employee> subordinates = new ArrayList<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
