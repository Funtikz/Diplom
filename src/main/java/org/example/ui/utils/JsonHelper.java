package org.example.ui.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import groovy.util.logging.Slf4j;

import java.util.*;
import java.util.stream.IntStream;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

@Slf4j
public class JsonHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Извлекает все значения по указанному полю из JSON массива
     *
     * @param jsonString JSON строка с массивом объектов
     * @param fieldName  имя поля для извлечения
     * @return List значений поля
     */
    public static List<String> extractFieldValues(String jsonString, String fieldName) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);

            if (!rootNode.isArray()) {
                log.warn("JSON не является массивом");
                return Collections.emptyList();
            }

            List<String> values = new ArrayList<>();
            for (JsonNode node : rootNode) {
                if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                    values.add(node.get(fieldName).asText());
                }
            }

            log.debug("Извлечено {} значений поля '{}'", values.size(), fieldName);
            return values;

        } catch (Exception e) {
            log.error("Ошибка при извлечении поля '{}' из JSON: {}", fieldName, e.getMessage(), e);
            throw new RuntimeException("Ошибка парсинга JSON", e);
        }
    }

    /**
     * Извлекает несколько полей из JSON массива
     *
     * @param jsonString JSON строка
     * @param fieldNames список имен полей
     * @return Map где ключ - имя поля, значение - List значений
     */
    public static Map<String, List<String>> extractMultipleFields(String jsonString, List<String> fieldNames) {
        Map<String, List<String>> result = new HashMap<>();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);

            if (!rootNode.isArray()) {
                log.warn("JSON не является массивом");
                return result;
            }

            for (String fieldName : fieldNames) {
                result.put(fieldName, new ArrayList<>());
            }

            for (JsonNode node : rootNode) {
                for (String fieldName : fieldNames) {
                    if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                        result.get(fieldName).add(node.get(fieldName).asText());
                    } else {
                        result.get(fieldName).add(null); // или можно пропустить
                    }
                }
            }

            log.debug("Извлечено {} полей из {} объектов", fieldNames.size(), rootNode.size());
            return result;

        } catch (Exception e) {
            log.error("Ошибка при извлечении полей из JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка парсинга JSON", e);
        }
    }


    /**
     * Находит индекс (позицию) элемента в списке по его имени
     * @param list список строк
     * @param name имя элемента для поиска
     * @return OptionalInt с индексом элемента, если найден, иначе пустой OptionalInt
     */
    public static OptionalInt findElementIndex(List<String> list, String name) {
        return IntStream.range(0, list.size())
                .filter(i -> list.get(i).equals(name))
                .findFirst();
    }

    /**
     * Удаляет первый объект из JSON массива.
     *
     * @param jsonArray JSON строка с массивом объектов
     * @return JSON строка без первого элемента массива
     * @throws Exception если JSON невалиден или не является массивом
     * Пример:
     * Вход:  [{"id":1}, {"id":2}, {"id":3}]
     * Выход: [{"id":2}, {"id":3}]
     */
    public static String removeFirstObjectFromJsonArray(String jsonArray) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonArray);

        if (rootNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) rootNode;

            if (!arrayNode.isEmpty()) {
                arrayNode.remove(0);
            }

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
        } else {
            throw new IllegalArgumentException("Представленный Json не является массивом");
        }
    }
}