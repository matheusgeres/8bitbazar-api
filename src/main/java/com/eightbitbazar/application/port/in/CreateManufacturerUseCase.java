package com.eightbitbazar.application.port.in;

import com.eightbitbazar.domain.manufacturer.Manufacturer;

public interface CreateManufacturerUseCase {

    Manufacturer execute(String name);
}
