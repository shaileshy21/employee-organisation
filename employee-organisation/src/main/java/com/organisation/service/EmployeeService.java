package com.organisation.service;

import com.organisation.dto.Employee;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmployeeService {

    private final Map<Integer, Employee> employees = new HashMap<>();

    /**
     * Method to load data from csv file into employees map
     * first load data into map
     * then create hierarchy by adding subordinated
     *
     * @param filePath path of csv file
     */
    public void loadDataFromFile(String filePath) {

        log.info("Loading data from CSV File...");

        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser parser = CSVFormat.Builder.create()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            parser.stream().forEach(this::addEmployee);
            buildHierarchyMap();
        } catch (Exception e) {
            log.error(" Error in parsing CSV file ", e);
        }
    }

    /**
     * method to insert csv record into map
     *
     * @param record CSV record read from the file
     */
    private void addEmployee(CSVRecord record) {
        try {
            var id = Integer.parseInt(record.get("Id"));
            var firstName = record.get("firstName");
            var lastName = record.get("lastName");
            var salary = new BigDecimal(record.get("salary"));
            var managerIdStr = record.get("managerId");

            var managerId = managerIdStr != null && !managerIdStr.isBlank()
                    ? Integer.parseInt(managerIdStr) : null;

            var employee = Employee.builder()
                    .id(id)
                    .firstName(firstName)
                    .lastName(lastName)
                    .salary(salary)
                    .managerId(managerId)
                    .build();

            employees.put(id, employee);

        } catch (Exception e) {
            log.error("Error parsing record: {}", record, e);
        }
    }

    /**
     * build the hierarchy of the employees
     * employee with no managerId will be the CEO;
     * employees having their managerId X will be added
     * as subordinates for the employee having ID as X
     */
    private void buildHierarchyMap() {
        for (var emp : employees.values()) {
            var managerId = emp.getManagerId();
            if (managerId != null) {
                var manager = employees.get(managerId);
                if (manager != null) {
                    manager.getSubordinates().add(emp);
                } else {
                    log.warn("Manager not found for employee: {}", emp.getFullName());
                }
            } else {
                log.info("Employee with ID: {} name: {} is the CEO.", emp.getId(), emp.getFullName());
            }
        }
    }

    /**
     * analyse the read data from the file
     */
    public void analyzeData() {
        log.info("Analyzing salary violations...");
        employees.values().stream()
                .filter(emp -> !emp.getSubordinates().isEmpty())
                .forEach(this::analyzeSalary);

        log.info("Analyzing reporting depth...");
        employees.values().forEach(this::checkReportingDepthLevel);
    }

    /**
     * method to analyse the salaries of each manager
     *
     * @param manager Manager Employee
     */
    private void analyzeSalary(Employee manager) {
        List<Employee> subs = manager.getSubordinates();
        if (subs.isEmpty()) return;

        // calculate totalSalary of all the subOrdinates
        BigDecimal total = subs.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int count = subs.size();
        BigDecimal avg = total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        BigDecimal min = avg.multiply(BigDecimal.valueOf(1.2)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal max = avg.multiply(BigDecimal.valueOf(1.5)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal managerSalary = manager.getSalary();

        if (managerSalary.compareTo(min) < 0) {
            log.info("Manager having ID: {} and name: {} earns Rs.{}; Rs.{} LESS than allowed (min: {})",
                    manager.getId(), manager.getFullName(), managerSalary, min.subtract(managerSalary), min);
        } else if (managerSalary.compareTo(max) > 0) {
            log.info("Manager having ID: {} and name: {} earns Rs.{}; Rs.{} MORE than allowed (max: {})",
                    manager.getId(), manager.getFullName(), managerSalary, managerSalary.subtract(max), max);
        }
    }

    /**
     * method to count the depth levels of each employee
     *
     * @param emp employee
     */
    private void checkReportingDepthLevel(Employee emp) {
        int depth = 0;
        Integer managerId = emp.getManagerId();

        while (managerId != null) {
            depth++;
            var manager = employees.get(managerId);
            if (manager == null) break;
            managerId = manager.getManagerId();
        }

        if (depth > 4) {
            log.info("Employee having ID: {} and name: {} has {} levels; {} levels above 4)",
                    emp.getId(), emp.getFullName(), depth, depth - 4);
        }
    }

    /**
     * method to get the employees map
     *
     * @return employee map
     */
    public Map<Integer, Employee> getEmployeesMap() {
        return employees;
    }
}
