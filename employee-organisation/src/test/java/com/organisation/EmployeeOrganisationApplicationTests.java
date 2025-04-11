package com.organisation;

import com.organisation.dto.Employee;
import com.organisation.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmployeeOrganisationApplicationTests {
    private EmployeeService employeeService;

    @BeforeEach
    void loadData() throws IOException {
        employeeService = new EmployeeService();
        File file = new ClassPathResource("employees.csv").getFile();
        employeeService.loadDataFromFile(file.getAbsolutePath());
    }

    @Test
    void testAllDataLoadedOrNotFromFile() {
        Map<Integer, Employee> map = employeeService.getEmployeesMap();
        assertEquals(15, map.size(), "Total Records loaded should have been 15");
    }

    @Test
    void testIfEmployeeHirarichyCreatedProperly() {
        var ceo = employeeService.getEmployeesMap().values().stream()
                .filter(e -> e.getManagerId() == null)
                .findFirst()
                .orElse(null);

        assertNotNull(ceo, "CEO should be present");
        assertEquals("Alice CEO", ceo.getFullName());
        assertFalse(ceo.getSubordinates().isEmpty(), "CEO should have subordinates");
    }

    @Test
    void testManagerIsPaidLessAmount() {
        var bobEmployee = employeeService.getEmployeesMap().get(2);
        assertNotNull(bobEmployee);
        var subs = bobEmployee.getSubordinates();
        assertFalse(subs.isEmpty());

        var totalSalaryofSubordinates = subs.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var avgSalaryOfSubordinates = totalSalaryofSubordinates.divide(
                BigDecimal.valueOf(subs.size()), 2, RoundingMode.HALF_UP);
        var minAllowedSalary = avgSalaryOfSubordinates.multiply(
                BigDecimal.valueOf(1.2)).setScale(2, RoundingMode.HALF_UP);

        assertTrue(bobEmployee.getSalary().compareTo(minAllowedSalary) < 0,
                "Bob should have been an underpaid employee");
    }

    @Test
    void testManagerIsPaidMoreAmount() {
        var carolEmp = employeeService.getEmployeesMap().get(3);
        assertNotNull(carolEmp);
        var subs = carolEmp.getSubordinates();
        assertFalse(subs.isEmpty());

        var totalSalaryofSubordinates = subs.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var avgSalaryOfSubordinates = totalSalaryofSubordinates.divide(
                BigDecimal.valueOf(subs.size()), 2, RoundingMode.HALF_UP);
        var maxAllowedSalary = avgSalaryOfSubordinates.multiply(
                BigDecimal.valueOf(1.5)).setScale(2, RoundingMode.HALF_UP);

        assertTrue(carolEmp.getSalary().compareTo(maxAllowedSalary) > 0,
                "Carol should have been an overpaid employee");
    }

    @Test
    void testReportingDepthLevelExceeding() {
        var deepEmployee = employeeService.getEmployeesMap().get(13);
        assertNotNull(deepEmployee);

        int depth = 0;
        var managerId = deepEmployee.getManagerId();

        while (managerId != null) {
            depth++;
            var manager = employeeService.getEmployeesMap().get(managerId);
            if (manager == null) break;
            managerId = manager.getManagerId();
        }

        assertTrue(depth > 4, "Employee with id : 13 should exceed reporting depth level(4)");
    }
}