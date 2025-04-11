package com.organisation;

import com.organisation.service.EmployeeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@SpringBootApplication
public class EmployeeOrganisationApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(EmployeeOrganisationApplication.class, args);
        EmployeeService employeeService = new EmployeeService();

        String path = new ClassPathResource("employees.csv").getFile().getAbsolutePath();
        employeeService.loadDataFromFile(path);
        employeeService.analyzeData();
    }

}
