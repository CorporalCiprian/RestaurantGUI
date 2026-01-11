package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.json.LegacyProdusListReader;
import org.example.json.ProdusJsonDto;
import org.example.json.ProdusJsonMapper;
import org.example.persistence.ProdusEntity;
import org.example.persistence.ProdusMapper;
import org.example.persistence.ProdusRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonMenuService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ProdusRepository produsRepository;

    public JsonMenuService() {
        this(new ProdusRepository());
    }

    public JsonMenuService(ProdusRepository produsRepository) {
        this.produsRepository = produsRepository;
    }

    public void exportMenu(File file) throws IOException {
        if (file == null) throw new IllegalArgumentException("file");
        List<ProdusJsonDto> dtos = produsRepository.findAll().stream()
                .map(ProdusMapper::toDomain)
                .map(ProdusJsonMapper::toDto)
                .toList();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, dtos);
    }

    public void importMenu(File file) throws IOException {
        if (file == null) throw new IllegalArgumentException("file");
        List<ProdusJsonDto> dtos = LegacyProdusListReader.readDtos(mapper, file);
        List<ProdusEntity> entities = dtos.stream()
                .map(ProdusJsonMapper::toDomain)
                .map(ProdusMapper::toEntity)
                .toList();

        produsRepository.replaceAll(entities);
    }
}
