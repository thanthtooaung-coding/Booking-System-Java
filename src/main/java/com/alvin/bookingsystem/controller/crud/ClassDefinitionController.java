package com.alvin.bookingsystem.controller.crud;

import com.alvin.bookingsystem.dto.request.ClassDefinitionFilter;
import com.alvin.bookingsystem.dto.request.ClassDefinitionRequest;
import com.alvin.bookingsystem.dto.request.PageAndFilterDTO;
import com.alvin.bookingsystem.dto.response.ApiResponse;
import com.alvin.bookingsystem.dto.response.ClassDefinitionResponse;
import com.alvin.bookingsystem.dto.response.PaginationDTO;
import com.alvin.bookingsystem.service.ClassDefinitionService;
import com.alvin.bookingsystem.util.ApiResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crud/class-definitions")
@io.swagger.v3.oas.annotations.tags.Tag(name = "CRUD Operations - Class Definitions", description = "CRUD operations for ClassDefinition entity")
public class ClassDefinitionController {

    private final ClassDefinitionService classDefinitionService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody ClassDefinitionRequest request, HttpServletRequest httpServletRequest) {
        ClassDefinitionResponse response = classDefinitionService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(response, "Class definition created successfully", httpServletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        ClassDefinitionResponse response = classDefinitionService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "Class definition retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @Valid @RequestBody ClassDefinitionRequest request, HttpServletRequest httpServletRequest) {
        ClassDefinitionResponse response = classDefinitionService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(response, "Class definition updated successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        classDefinitionService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.success(null, "Class definition deleted successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse> getAll(@RequestBody PageAndFilterDTO<ClassDefinitionFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        PaginationDTO<ClassDefinitionResponse> response = classDefinitionService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(response, "Class definitions retrieved successfully", httpServletRequest);
        return ResponseEntity.ok(apiResponse);
    }
}
