package com.pelni.boarding.ticket.util;

import com.pelni.boarding.ticket.dto.AdminDto;
import com.pelni.boarding.ticket.dto.AnotherDataDto;
import com.pelni.boarding.ticket.entity.LogNew;
import com.pelni.boarding.ticket.repository.LogNewRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Base64;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class UtilFn {

    private static final Logger log = LoggerFactory.getLogger(UtilFn.class);

    public static AdminDto getDataFromToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            if (authorizationHeader != null) {
                String token = authorizationHeader.substring(7);

                String[] tokenParts = token.split("\\.");
                String encodedPayload = tokenParts[1];

                byte[] decodedPayload = Base64.getDecoder().decode(encodedPayload);
                String payload = new String(decodedPayload);

                JSONObject payloadJson = new JSONObject(payload);
                log.info("payload: {}", payloadJson);
                return AdminDto.builder()
                        .username(payloadJson.getString("username"))
                        .name(payloadJson.getString("name"))
                        .role(payloadJson.getString("role"))
                        .type(payloadJson.getString("type"))
                        .printer(payloadJson.getString("printer"))
                        .anotherData(AnotherDataDto.builder()
                                .userId(payloadJson.getJSONObject("anotherData").getString("userId"))
                                .firstName(payloadJson.getJSONObject("anotherData").getString("firstName"))
                                .phoneNumber(payloadJson.getJSONObject("anotherData").getString("phoneNumber"))
                                .email(payloadJson.getJSONObject("anotherData").getString("email"))
                                .office(payloadJson.getJSONObject("anotherData").getString("office"))
                                .branch(payloadJson.getJSONObject("anotherData").getString("branch"))
                                .data(payloadJson.getJSONObject("anotherData").getString("data"))
                                .build())
                        .build();
            }
        }
        return null;
    }

    private static String getClientIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        }
        return xForwardedForHeader.split(",")[0].trim();
    }

    public static void saveLog(String username, String level, String activity, String request, String response,
                               String vm, LogNewRepository logNewRepository) {
        logNewRepository.save(LogNew.builder()
                .username(username)
                .level(level)
                .activity(activity)
                .request(request)
                .response(response)
                .ipAddress(getClientIpAddress())
                .datetime(LocalDateTime.now())
                .vm(vm)
                .build());

    }
}
