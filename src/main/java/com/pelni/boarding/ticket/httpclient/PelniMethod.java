package com.pelni.boarding.ticket.httpclient;

import lombok.Getter;
import org.springframework.http.HttpMethod;

@Getter
public enum PelniMethod {
    LOGIN("/web-admin/user-login", HttpMethod.GET),
    CHECK("/web-dcs/get-checkin-info", HttpMethod.GET),
    PRINT("/web-dcs/checkin", HttpMethod.GET);

    private final String url;
    private final HttpMethod httpMethod;

    PelniMethod(String url, HttpMethod httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod;
    }
}
