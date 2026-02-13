package com.example.monolith_service.employee;

import com.example.monolith_service.employee.dto.EmployeeRequest;
import com.example.monolith_service.employee.dto.EmployeePageResponse;
import com.example.monolith_service.employee.dto.EmployeeResponse;
import com.example.monolith_service.error.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public EmployeeResponse create(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName().trim());
        employee.setLastName(request.getLastName().trim());
        employee.setEmail(request.getEmail().trim().toLowerCase());
        return toResponse(employeeRepository.save(employee));
    }

    public EmployeePageResponse getAll(int page, int size, String sortBy, String direction, String search) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Sort sort = "desc".equalsIgnoreCase(direction)
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        String normalizedSearch = search == null ? "" : search.trim();

        Page<Employee> employeePage;
        if (normalizedSearch.isEmpty()) {
            employeePage = employeeRepository.findAll(pageable);
        } else {
            employeePage = employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                normalizedSearch,
                normalizedSearch,
                normalizedSearch,
                pageable
            );
        }

        return new EmployeePageResponse(
            employeePage.getContent().stream().map(this::toResponse).toList(),
            employeePage.getNumber(),
            employeePage.getSize(),
            employeePage.getTotalElements(),
            employeePage.getTotalPages(),
            employeePage.hasNext(),
            employeePage.hasPrevious()
        );
    }

    public EmployeeResponse getById(Long id) {
        Employee employee = findOrThrow(id);
        return toResponse(employee);
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findOrThrow(id);
        employee.setFirstName(request.getFirstName().trim());
        employee.setLastName(request.getLastName().trim());
        employee.setEmail(request.getEmail().trim().toLowerCase());
        return toResponse(employeeRepository.save(employee));
    }

    public void delete(Long id) {
        Employee employee = findOrThrow(id);
        employeeRepository.delete(employee);
    }

    private Employee findOrThrow(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
            employee.getId(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getEmail()
        );
    }
}
