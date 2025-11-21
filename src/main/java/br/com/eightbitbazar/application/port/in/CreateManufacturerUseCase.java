package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.domain.manufacturer.Manufacturer;

public interface CreateManufacturerUseCase {

    Manufacturer execute(String name);
}
