package org.example.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Pizza;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads either the new DTO format (with "tip") or legacy files (without "tip").
 */
public final class LegacyProdusListReader {
    private LegacyProdusListReader() {
    }

    public static List<ProdusJsonDto> readDtos(ObjectMapper mapper, File file) throws IOException {
        JsonNode root = mapper.readTree(file);
        if (root == null || !root.isArray()) return List.of();

        List<ProdusJsonDto> out = new ArrayList<>();
        for (JsonNode n : root) {
            if (n == null || n.isNull()) continue;

            // New format: contains "tip"
            if (n.hasNonNull("tip")) {
                String tip = n.get("tip").asText("");
                ProdusJsonDto dto = switch (tip.toUpperCase()) {
                    case "PIZZA" -> mapper.convertValue(n, PizzaJsonDto.class);
                    case "MANCARE" -> mapper.convertValue(n, MancareJsonDto.class);
                    case "BAUTURA" -> mapper.convertValue(n, BauturaJsonDto.class);
                    default -> mapper.convertValue(n, MancareJsonDto.class);
                };
                if (dto != null) out.add(dto);
                continue;
            }

            // Legacy: infer
            String nume = text(n, "nume");
            double pret = number(n, "pret", 0.0);
            boolean vegetarian = bool(n, "vegetarian", false);

            Integer gramaj = intOrNull(n, "gramaj");
            Integer volum = intOrNull(n, "volum");

            Pizza.TipBlat blat = enumOrNull(n, "blat", Pizza.TipBlat.class);
            Pizza.TipSos sos = enumOrNull(n, "sos", Pizza.TipSos.class);
            List<Pizza.Topping> toppings = toppings(n, mapper);

            out.add(ProdusJsonMapper.inferDto(nume, pret, vegetarian, gramaj, volum, blat, sos, toppings));
        }
        return out;
    }

    private static String text(JsonNode n, String field) {
        JsonNode v = n.get(field);
        return v != null && !v.isNull() ? v.asText() : "";
    }

    private static double number(JsonNode n, String field, double def) {
        JsonNode v = n.get(field);
        return v != null && v.isNumber() ? v.asDouble() : def;
    }

    private static boolean bool(JsonNode n, String field, boolean def) {
        JsonNode v = n.get(field);
        return v != null && v.isBoolean() ? v.asBoolean() : def;
    }

    private static Integer intOrNull(JsonNode n, String field) {
        JsonNode v = n.get(field);
        return v != null && v.isNumber() ? v.asInt() : null;
    }

    private static <E extends Enum<E>> E enumOrNull(JsonNode n, String field, Class<E> enumType) {
        JsonNode v = n.get(field);
        if (v == null || v.isNull()) return null;
        try {
            return Enum.valueOf(enumType, v.asText());
        } catch (Exception ex) {
            return null;
        }
    }

    private static List<Pizza.Topping> toppings(JsonNode n, ObjectMapper mapper) {
        JsonNode t = n.get("toppings");
        if (t == null || !t.isArray()) return null;
        try {
            return mapper.convertValue(t, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return null;
        }
    }
}
