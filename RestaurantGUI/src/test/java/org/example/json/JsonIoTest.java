package org.example.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonIoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void exportWritesTipDiscriminator() throws Exception {
        List<Produs> produse = List.of(
                new Mancare("Paste", 10.0, 200, false),
                new Bautura("Apa", 5.0, 500, true),
                new Pizza.Builder(Pizza.TipBlat.CLASIC, Pizza.TipSos.ROSU).nume("P1").baza(30).build()
        );

        List<ProdusJsonDto> dtos = produse.stream().map(ProdusJsonMapper::toDto).toList();

        File tmp = File.createTempFile("meniu-", ".json");
        tmp.deleteOnExit();
        mapper.writerWithDefaultPrettyPrinter().writeValue(tmp, dtos);

        JsonNode root = mapper.readTree(tmp);
        assertTrue(root.isArray());
        assertEquals(3, root.size());
        for (JsonNode n : root) {
            assertTrue(n.hasNonNull("tip"), "DTO JSON must contain 'tip'");
        }
    }

    @Test
    void legacyFileWithoutTipIsStillReadable() throws Exception {
        // uses workspace meniu-export.json (legacy format in this repo)
        File legacy = new File("meniu-export.json");
        assertTrue(legacy.exists(), "Expected meniu-export.json in project root");

        List<ProdusJsonDto> dtos = LegacyProdusListReader.readDtos(mapper, legacy);
        assertFalse(dtos.isEmpty());

        // should infer at least one pizza + one food/drink
        boolean hasPizza = dtos.stream().anyMatch(d -> d instanceof PizzaJsonDto);
        boolean hasMancare = dtos.stream().anyMatch(d -> d instanceof MancareJsonDto);
        boolean hasBautura = dtos.stream().anyMatch(d -> d instanceof BauturaJsonDto);
        assertTrue(hasPizza || hasMancare || hasBautura);

        // and mapping to domain should not throw
        List<Produs> produse = dtos.stream().map(ProdusJsonMapper::toDomain).toList();
        assertEquals(dtos.size(), produse.size());

        // sanity: ensure file itself had no 'tip' fields
        String text = Files.readString(legacy.toPath());
        assertFalse(text.contains("\"tip\""), "Legacy file should not contain tip; otherwise this test becomes redundant");
    }
}

