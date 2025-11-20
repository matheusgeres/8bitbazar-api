package com.eightbitbazar.application.port.in;

import com.eightbitbazar.domain.manufacturer.Manufacturer;

import java.util.List;

public interface ListManufacturersUseCase {

    List<Manufacturer> execute();
}
