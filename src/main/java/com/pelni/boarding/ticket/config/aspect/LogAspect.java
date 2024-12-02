package com.pelni.boarding.ticket.config.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@Configuration
@RequiredArgsConstructor
public class LogAspect implements Filter {

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    private static final ThreadLocal<String> classWithPackageName = new ThreadLocal<>();
    private static final ThreadLocal<String> methodClassName = new ThreadLocal<>();
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Pattern WORKBASKET_PATTERN = Pattern.compile("^/work-basket/documents/?$");
    private static final Pattern ORION_PATTERN = Pattern.compile("^/orion/documents/?$");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @After("execution(* com.pelni.boarding.ticket.service.impl.*.*(..)) || execution(* com.pelni.boarding.ticket.vo.*.*(..))")
    public void getClassWithPackageName(JoinPoint jp) {
        classWithPackageName.set(jp.getTarget().getClass().getName());
        methodClassName.set(jp.getSignature().getName());
        List<Object> argsList = Arrays.asList(jp.getArgs());
        var jsonArgsList = argsList.stream()
                .map(this::convertToJson)
                .collect(Collectors.toList());
        log.debug("Arguments: {}", String.join(",", jsonArgsList));
    }

    private String convertToJson(Object arg) {
        try {
            return objectMapper.writeValueAsString(arg);
        } catch (Exception e) {
            log.error("Error converting argument to JSON: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        if (req.getRequestURI().contains("swagger-ui") || req.getRequestURI().contains("v3/api-docs")) {
            log.info("Request for Swagger UI detected, forwarding to Swagger.");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(req);
        ResponseWrapper responseWrapper = new ResponseWrapper(resp);

        filterChain.doFilter(requestWrapper, responseWrapper);
        responseWrapper.flushBuffer();

        byte[] requestPayload = requestWrapper.getContentAsByteArray();
        byte[] responsePayload = responseWrapper.getCopy();

        logRequestDetails(requestWrapper, resp, requestPayload, responsePayload);

        ServletOutputStream outputStream = resp.getOutputStream();
        outputStream.write(responsePayload);
        outputStream.flush();
    }

    private void logRequestDetails(ContentCachingRequestWrapper req, HttpServletResponse resp, byte[] requestPayload, byte[] responsePayload) throws IOException {
        String api = req.getServletPath();
        String methodName = req.getMethod();
        String className = extractClassName(classWithPackageName.get());
        String identifier = UUID.randomUUID().toString();
        String ipAddress = getClientIpAddress(req);
        LocalDateTime dateTime = LocalDateTime.now();
        String remoteAddress = req.getRemoteAddr();

        Map<String, String> queryParams = getQueryParams(req);
        Map<String, String> requestParams = getRequestParams(req);

        log.info("remoteAddress : {}", remoteAddress);
        log.info("\napi : {}\nmethod : {}\nclassWithPackageName :  {}\nclassName : {}\nmethodClassName : {}\nidentifier: {}",
                api, methodName, classWithPackageName.get(), className, methodClassName.get(), identifier);
        log.info("Query Parameters: {}", queryParams);
        log.info("Request Parameters: {}", requestParams);

        String requestBody = new String(requestPayload, req.getCharacterEncoding());
        String responseBody = new String(responsePayload, resp.getCharacterEncoding());

        if (WORKBASKET_PATTERN.matcher(api).matches() || ORION_PATTERN.matcher(api).matches()) {
            requestBody = "-";
            responseBody = "-";
        }

        log.info("Request payload: {}", requestBody);

        if (resp.getStatus() != 200 && resp.getStatus() != 201) {
            log.error("Response error : {}", responseBody);
            String finalRsp = extractMessageFromResponse(responseBody);
            sendErrorToDiscord(identifier, api, methodName, className, methodClassName.get(), classWithPackageName.get(),
                    finalRsp, ipAddress, dateTime, requestBody, queryParams, requestParams);
        } else {
            log.info("Response success : {}", responseBody);
        }
    }

    private void sendErrorToDiscord(String identifier, String api, String methodName, String className,
                                    String methodClassName, String classWithPackageName, String errorDescription,
                                    String ipAddress, LocalDateTime dateTime, String requestPayload,
                                    Map<String, String> queryParams, Map<String, String> requestParams) {
        CompletableFuture.runAsync(() -> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                String formattedPayload = formatPayloadAsJson(requestPayload);
                String formattedQueryParams = formatMapAsJson(queryParams);
                String formattedRequestParams = formatMapAsJson(requestParams);

                String content = String.format("""
                    **Error Details:**
                    🆔 **ID:** %s
                    🔗 **URL:** %s
                    🛠 **Method:** %s
                    📦 **Payload:** %s
                    🔍 **Query Parameters:** %s
                    🔢 **Request Parameters:** %s
                    📂 **Class:** %s
                    🧩 **Method Class Name:** %s
                    📍 **Location:** %s
                    ⚠️ **Description:** %s
                    🌐 **IP Address:** %s
                    ⏰ **Created At:** %s
                    """,
                        identifier,
                        api,
                        methodName,
                        formattedPayload,
                        formattedQueryParams,
                        formattedRequestParams,
                        className,
                        methodClassName,
                        classWithPackageName,
                        errorDescription,
                        ipAddress,
                        dateTime.format(formatter)
                );

                String requestBody = objectMapper.writeValueAsString(new DiscordMessage(content));
                HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
                restTemplate.postForObject(webhookUrl, request, String.class);
            } catch (Exception e) {
                log.error("Failed to send error to Discord", e);
            }
        });
    }

    private static String extractClassName(String packageName) {
        if (packageName == null) return "";
        int lastDotIndex = packageName.lastIndexOf('.');
        return lastDotIndex == -1 ? packageName : packageName.substring(lastDotIndex + 1);
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}

    private String extractMessageFromResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("message");
        } catch (JSONException e) {
            return response;
        }
    }

    private static class DiscordMessage {
        public String content;

        public DiscordMessage(String content) {
            this.content = content;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        }
        return xForwardedForHeader.split(",")[0].trim();
    }

    private String formatPayloadAsJson(String payload) {
        try {
            JSONObject jsonPayload = new JSONObject(payload);
            return "```json\n" + jsonPayload.toString(2) + "\n```";
        } catch (JSONException e) {
            return payload;
        }
    }

    private String formatMapAsJson(Map<String, String> map) {
        try {
            JSONObject jsonObject = new JSONObject(map);
            return "```json\n" + jsonObject.toString(2) + "\n```";
        } catch (JSONException e) {
            return map.toString();
        }
    }

    private Map<String, String> getQueryParams(HttpServletRequest request) {
        Map<String, String> queryParams = new HashMap<>();
        String queryString = request.getQueryString();
        if (queryString != null) {
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                queryParams.put(pair.substring(0, idx), pair.substring(idx + 1));
            }
        }
        return queryParams;
    }

    private Map<String, String> getRequestParams(HttpServletRequest request) {
        Map<String, String> requestParams = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            requestParams.put(paramName, paramValue);
        }
        return requestParams;
    }
}