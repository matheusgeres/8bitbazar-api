package com.eightbitbazar.adapter.in.web;

import com.eightbitbazar.application.port.in.*;
import com.eightbitbazar.domain.manufacturer.Manufacturer;
import com.eightbitbazar.domain.platform.Platform;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final CreatePlatformUseCase createPlatformUseCase;
    private final ListPlatformsUseCase listPlatformsUseCase;
    private final CreateManufacturerUseCase createManufacturerUseCase;
    private final ListManufacturersUseCase listManufacturersUseCase;

    public AdminController(
        CreatePlatformUseCase createPlatformUseCase,
        ListPlatformsUseCase listPlatformsUseCase,
        CreateManufacturerUseCase createManufacturerUseCase,
        ListManufacturersUseCase listManufacturersUseCase
    ) {
        this.createPlatformUseCase = createPlatformUseCase;
        this.listPlatformsUseCase = listPlatformsUseCase;
        this.createManufacturerUseCase = createManufacturerUseCase;
        this.listManufacturersUseCase = listManufacturersUseCase;
    }

    @PostMapping("/platforms")
    public ResponseEntity<PlatformResponse> createPlatform(@Valid @RequestBody CreateRequest request) {
        Platform platform = createPlatformUseCase.execute(request.name());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new PlatformResponse(platform.id(), platform.name()));
    }

    @GetMapping("/platforms")
    public ResponseEntity<List<PlatformResponse>> listPlatforms() {
        List<PlatformResponse> platforms = listPlatformsUseCase.execute().stream()
            .map(p -> new PlatformResponse(p.id(), p.name()))
            .toList();
        return ResponseEntity.ok(platforms);
    }

    @PostMapping("/manufacturers")
    public ResponseEntity<ManufacturerResponse> createManufacturer(@Valid @RequestBody CreateRequest request) {
        Manufacturer manufacturer = createManufacturerUseCase.execute(request.name());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ManufacturerResponse(manufacturer.id(), manufacturer.name()));
    }

    @GetMapping("/manufacturers")
    public ResponseEntity<List<ManufacturerResponse>> listManufacturers() {
        List<ManufacturerResponse> manufacturers = listManufacturersUseCase.execute().stream()
            .map(m -> new ManufacturerResponse(m.id(), m.name()))
            .toList();
        return ResponseEntity.ok(manufacturers);
    }

    public record CreateRequest(
        @NotBlank(message = "Name is required")
        String name
    ) {}

    public record PlatformResponse(Long id, String name) {}

    public record ManufacturerResponse(Long id, String name) {}
}
