package br.com.eightbitbazar.application.port.out;

import br.com.eightbitbazar.domain.manufacturer.Manufacturer;

import java.util.List;
import java.util.Optional;

public interface ManufacturerRepository {

    Manufacturer save(Manufacturer manufacturer);

    Optional<Manufacturer> findById(Long id);

    Optional<Manufacturer> findByName(String name);

    List<Manufacturer> findAll();

    boolean existsByName(String name);
}
