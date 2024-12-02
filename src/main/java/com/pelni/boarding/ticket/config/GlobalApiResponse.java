package com.pelni.boarding.ticket.config;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

import static com.pelni.boarding.ticket.util.MyConstants.*;


@SuppressWarnings("ALL")
@Slf4j
public class GlobalApiResponse<T> extends ResponseEntity<T> {
    public GlobalApiResponse(T data, HttpStatus status) {
        super(
                (T) new HashMap<String, Object>() {{
                    put(TIMESTAMP, System.currentTimeMillis());
                    put(STATUS_CODE, status.value());
                    put(STATUS_MESSAGE, status.getReasonPhrase());
                    put(MESSAGE, status.is2xxSuccessful() ? SUCCESS : status.getReasonPhrase());
                    put(DATA, data);
                }}
                , status);
    }

    public GlobalApiResponse(HttpStatus status) {
        super(
                (T) new HashMap<String, Object>() {{
                    put(TIMESTAMP, System.currentTimeMillis());
                    put(STATUS_CODE, status.value());
                    put(STATUS_MESSAGE, status.getReasonPhrase());
                    put(MESSAGE, status.is2xxSuccessful() ? SUCCESS : status.getReasonPhrase());
                    put(DATA, null);
                }}
                , status);
    }

    public GlobalApiResponse(String message, HttpStatus status) {
        super(
                (T) new HashMap<String, Object>() {{
                    put(TIMESTAMP, System.currentTimeMillis());
                    put(STATUS_CODE, status.value());
                    put(STATUS_MESSAGE, status.getReasonPhrase());
                    put(MESSAGE, message);
                    put(DATA, null);
                }}
                , status);
    }

    public GlobalApiResponse(Object errors, String message, HttpStatus status) {
        super(
                (T) new HashMap<String, Object>() {{
                    put(TIMESTAMP, System.currentTimeMillis());
                    put(STATUS_CODE, status.value());
                    put(STATUS_MESSAGE, status.getReasonPhrase());
                    put(MESSAGE, message == null ? status.getReasonPhrase() : message);
                    put(DATA, null);
                }}, status);
    }

    public String toJson() {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(this.getBody());
        } catch (JsonProcessingException e) {
            return "{}";
        }

    }
}
