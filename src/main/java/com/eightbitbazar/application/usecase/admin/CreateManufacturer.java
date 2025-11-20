package com.eightbitbazar.application.usecase.admin;

import com.eightbitbazar.application.port.in.CreateManufacturerUseCase;
import com.eightbitbazar.application.port.out.ManufacturerRepository;
import com.eightbitbazar.domain.exception.BusinessException;
import com.eightbitbazar.domain.manufacturer.Manufacturer;

import java.time.LocalDateTime;

public class CreateManufacturer implements CreateManufacturerUseCase {

    private final ManufacturerRepository manufacturerRepository;

    public CreateManufacturer(ManufacturerRepository manufacturerRepository) {
        this.manufacturerRepository = manufacturerRepository;
    }

    @Override
    public Manufacturer execute(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Manufacturer name is required");
        }

        if (manufacturerRepository.existsByName(name)) {
            throw new BusinessException("Manufacturer already exists");
        }

        Manufacturer manufacturer = new Manufacturer(null, name, LocalDateTime.now());
        return manufacturerRepository.save(manufacturer);
    }
}
