package com.alvin.bookingsystem.controller.crud;

import com.alvin.bookingsystem.dto.request.ClassScheduleFilter;
import com.alvin.bookingsystem.dto.request.ClassScheduleRequest;
import com.alvin.bookingsystem.dto.request.PageAndFilterDTO;
import com.alvin.bookingsystem.dto.response.ApiResponse;
import com.alvin.bookingsystem.dto.response.ClassScheduleResponse;
import com.alvin.bookingsystem.dto.response.PaginationDTO;
import com.alvin.bookingsystem.service.ClassScheduleService;
import com.alvin.bookingsystem.util.ApiResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crud/class-schedules")
@io.swagger.v3.oas.annotations.tags.Tag(name = "CRUD Operations - Class Schedules", description = "CRUD operations for ClassSchedule entity")
public class ClassScheduleController {

    private final ClassScheduleService classScheduleService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody ClassScheduleRequest request, HttpServletRequest httpServletRequest) {
        ClassScheduleResponse response = classScheduleService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(response, "Class schedule created successfully", httpServletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        ClassScheduleResponse response = classScheduleService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "Class schedule retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @Valid @RequestBody ClassScheduleRequest request, HttpServletRequest httpServletRequest) {
        ClassScheduleResponse response = classScheduleService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "Class schedule updated successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        classScheduleService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.success(null, "Class schedule deleted successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse> getAll(@RequestBody PageAndFilterDTO<ClassScheduleFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        PaginationDTO<ClassScheduleResponse> response = classScheduleService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(response, "Class schedules retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }
}
