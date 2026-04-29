package com.connectwork.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Proporciona una instancia única y configurada de Gson para todo el proyecto.
 * Maneja correctamente los tipos LocalDate y LocalDateTime de Java 17+,
 * que por defecto Gson no puede serializar/deserializar debido a las
 * restricciones de acceso a módulos del JDK.
 */
public class GsonUtil {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Gson INSTANCIA = new GsonBuilder()
            // LocalDate
            .registerTypeAdapter(LocalDate.class,
                    (com.google.gson.JsonSerializer<LocalDate>)
                    (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class,
                    (com.google.gson.JsonDeserializer<LocalDate>)
                    (json, type, ctx) -> LocalDate.parse(json.getAsString()))

            // LocalDateTime
            .registerTypeAdapter(LocalDateTime.class,
                    (com.google.gson.JsonSerializer<LocalDateTime>)
                    (src, type, ctx) -> new JsonPrimitive(src.format(FORMATO_FECHA_HORA)))
            .registerTypeAdapter(LocalDateTime.class,
                    (com.google.gson.JsonDeserializer<LocalDateTime>)
                    (json, type, ctx) -> LocalDateTime.parse(json.getAsString(), FORMATO_FECHA_HORA))

            .setPrettyPrinting()
            .create();

    /**
     * Obtiene la instancia configurada de Gson para el proyecto.
     */
    public static Gson getGson() {
        return INSTANCIA;
    }
}
