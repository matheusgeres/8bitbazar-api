package com.eightbitbazar.application.usecase.admin;

import com.eightbitbazar.application.port.in.ListManufacturersUseCase;
import com.eightbitbazar.application.port.out.ManufacturerRepository;
import com.eightbitbazar.domain.manufacturer.Manufacturer;

import java.util.List;

public class ListManufacturers implements ListManufacturersUseCase {

    private final ManufacturerRepository manufacturerRepository;

    public ListManufacturers(ManufacturerRepository manufacturerRepository) {
        this.manufacturerRepository = manufacturerRepository;
    }

    @Override
    public List<Manufacturer> execute() {
        return manufacturerRepository.findAll();
    }
}
