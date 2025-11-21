package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.domain.manufacturer.Manufacturer;

import java.util.List;

public interface ListManufacturersUseCase {

    List<Manufacturer> execute();
}
