package com.pelni.boarding.ticket.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pelni.boarding.ticket.config.exception.CustomException;
import com.pelni.boarding.ticket.config.security.JwtToken;
import com.pelni.boarding.ticket.dto.*;
import com.pelni.boarding.ticket.entity.Admin;
import com.pelni.boarding.ticket.entity.CheckinInfo;
import com.pelni.boarding.ticket.httpclient.PelniMethod;
import com.pelni.boarding.ticket.repository.AdminRepository;
import com.pelni.boarding.ticket.repository.CheckinInfoRepository;
import com.pelni.boarding.ticket.repository.LogNewRepository;
import com.pelni.boarding.ticket.service.PelniService;
import com.pelni.boarding.ticket.util.UtilFn;
import com.pelni.boarding.ticket.vo.LoginVo;
import com.pelni.boarding.ticket.vo.PrintVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.pelni.boarding.ticket.util.MyConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PelniServiceImpl implements PelniService {

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.rq-id}")
    private String rqId;

    @Value("${app.username}")
    private String username;

    @Value("${app.password}")
    private String password;

    private final AdminRepository adminRepository;
    private final LogNewRepository logNewRepository;
    private final CheckinInfoRepository checkinInfoRepository;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;
    private final JwtToken jwtToken;

    @Override
    public LoginDto login(LoginVo vo) {
        Admin admin = adminRepository.findByUsername(vo.getUsername())
                .orElseThrow(() -> new CustomException(INVALID_USERNAME, HttpStatus.BAD_REQUEST));

        if (!admin.getPassword().equals(vo.getPassword())) {
            throw new CustomException(INVALID_PASSWORD, HttpStatus.BAD_REQUEST);
        }
        String url = String.format("%s%s?rqid=%s&username=%s&password=%s",
                baseUrl, PelniMethod.LOGIN.getUrl(), rqId, username, password);
        log.info("Request Login URL: {} with method: {}", url, PelniMethod.LOGIN.getHttpMethod());

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url, PelniMethod.LOGIN.getHttpMethod(), null, String.class
        );

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.error("request login not 200: {}", responseEntity.getBody());
            throw new CustomException(responseEntity.getBody(), (HttpStatus) responseEntity.getStatusCode());
        }

        String response = responseEntity.getBody();
        log.info("Response Login: {}", response);

        UtilFn.saveLog(
                admin.getUsername(), admin.getRole().toUpperCase(), LOGIN, url, response, admin.getType().toUpperCase(),
                logNewRepository
        );

        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            log.error("error parse json: {}", e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        if (!"0".equals(jsonNode.path("error_code").asText())) {
            String errorMessage = jsonNode.path("error_message").asText();
            log.error("Login failed: {}", errorMessage);
            throw new CustomException(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        AdminDto adminDto = AdminDto.builder()
                .username(admin.getUsername())
                .name(admin.getName())
                .role(admin.getRole())
                .type(admin.getType())
                .printer(admin.getPrinter())
                .anotherData(AnotherDataDto.builder()
                        .userId(jsonNode.path("USER_ID").asText())
                        .firstName(jsonNode.path("FIRST_NAME").asText())
                        .phoneNumber(jsonNode.path("PHONE_NUMBER").asText())
                        .email(jsonNode.path("EMAIL").asText())
                        .office(jsonNode.path("OFFICE").asText())
                        .branch(jsonNode.path("BRANCH").asText())
                        .data(jsonNode.path("TOKEN").asText())
                        .build())
                .build();

        return new LoginDto(jwtToken.generateToken(adminDto));
    }

    @Override
    public List<CheckinInfoDto> check(String bookingCode) {
        var data = UtilFn.getDataFromToken();

        String url = String.format("%s%s?rqid=%s&user_id=%s&token=%s&book_code=%s",
                baseUrl, PelniMethod.CHECK.getUrl(), rqId, Objects.requireNonNull(data).getAnotherData().getUserId(),
                data.getAnotherData().getData(), bookingCode);
        log.info("Request Check Info URL: {} with method: {}", url, PelniMethod.CHECK.getHttpMethod());

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url, PelniMethod.CHECK.getHttpMethod(), null, String.class
        );

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.error("Request check info not 200: {}", responseEntity.getBody());
            throw new CustomException(responseEntity.getBody(), (HttpStatus) responseEntity.getStatusCode());
        }

        String response = responseEntity.getBody();
        log.info("Response Check Info: {}", response);

        UtilFn.saveLog(
                data.getUsername(), data.getRole().toUpperCase(), String.format(GET_CHECKIN_INFO, bookingCode), url,
                response, data.getType().toUpperCase(), logNewRepository
        );

        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            log.error("error parse json check info: {}", e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        if (!"0".equals(jsonNode.path("error_code").asText())) {
            String errorMessage = jsonNode.path("error_message").asText();
            log.error("Check info failed: {}", errorMessage);
            if (jsonNode.path("error_code").asText().equals("500106")) {
                throw new CustomException(String.format(NOT_FOUND, bookingCode) , HttpStatus.NOT_FOUND);
            } else if (jsonNode.path("error_code").asText().equals("550004")) {
                throw new CustomException(String.format(MAXIMUM_TIME, bookingCode) , HttpStatus.BAD_REQUEST);
            } else {
                throw new CustomException(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        checkinInfoRepository.deleteAll(checkinInfoRepository.findByBookCode(bookingCode));

        ArrayNode paxList = (ArrayNode) jsonNode.path("PAX_LIST");
        if (paxList.isArray()) {
            for (JsonNode pax : paxList) {
                CheckinInfo checkinInfoDb = new CheckinInfo();
                checkinInfoDb.setNumberCode(jsonNode.path("NUM_CODE").asText());
                checkinInfoDb.setBookingCode(jsonNode.path("BOOK_CODE").asText());
                checkinInfoDb.setShipNumber(jsonNode.path("SHIP_NO").asText());
                checkinInfoDb.setShipName(jsonNode.path("SHIP_NAME").asText());
                checkinInfoDb.setAge(jsonNode.path("VOYAGE_NO").asText());
                checkinInfoDb.setYear(jsonNode.path("YEAR").asText());
                checkinInfoDb.setOrganizationCode(jsonNode.path("ORG").asText());
                checkinInfoDb.setOrganizationName(jsonNode.path("ORG_NAME").asText());
                checkinInfoDb.setOrganizationCall(jsonNode.path("ORG_CALL").asText());
                checkinInfoDb.setDestinationCode(jsonNode.path("DES").asText());
                checkinInfoDb.setDestinationName(jsonNode.path("DES_NAME").asText());
                checkinInfoDb.setDestinationCall(jsonNode.path("DES_CALL").asText());
                checkinInfoDb.setDepartureDate(LocalDate.parse(jsonNode.path("DEP_DATE").asText(),
                        DateTimeFormatter.ofPattern("yyyyMMdd")));
                checkinInfoDb.setDepartureTime(jsonNode.path("DEP_TIME").asText());
                checkinInfoDb.setArrivalDate(LocalDate.parse(jsonNode.path("ARV_DATE").asText(),
                        DateTimeFormatter.ofPattern("yyyyMMdd")));
                checkinInfoDb.setArrivalTime(jsonNode.path("ARV_TIME").asText());
                checkinInfoDb.setSubclass(jsonNode.path("SUBCLASS").asText());
                checkinInfoDb.setMaxCheckin(jsonNode.path("MAX_CHECKIN").asText());
                checkinInfoDb.setMinCheckin(jsonNode.path("MIN_CHECKIN").asText());
                checkinInfoDb.setClassType(jsonNode.path("CLASS").asText());
                checkinInfoDb.setFamily(jsonNode.path("FAMILY").asText());
                checkinInfoDb.setStatus(jsonNode.path("STATUS").asText());
                checkinInfoDb.setTransactionDate(jsonNode.path("TRANSACTION_DATE").asText());
                checkinInfoDb.setLocation(jsonNode.path("LOCATION").asText());
                checkinInfoDb.setName(pax.get(0).asText());
                checkinInfoDb.setIdentityNumber(pax.get(1).asText());
                checkinInfoDb.setAdult(pax.get(2).asText());
                checkinInfoDb.setDeck(pax.get(3).asText());
                checkinInfoDb.setCabin(pax.get(4).asText());
                checkinInfoDb.setBed(pax.get(5).asText());
                checkinInfoDb.setGender(pax.get(6).asText());
                checkinInfoDb.setTicketNumber(pax.get(7).asText());
                checkinInfoDb.setTicketPrinted(Integer.valueOf(pax.get(8).asText()));
                checkinInfoDb.setMaxPrintTicket(pax.get(9).asInt());
                checkinInfoDb.setPrice(pax.get(10).asText());
                checkinInfoDb.setAdminFee(pax.get(11).asText());
                checkinInfoDb.setNetPrice(pax.get(12).asText());
                checkinInfoDb.setVaccine(pax.get(13).asText());
                checkinInfoDb.setAntigenTest(pax.get(14).asText());
                checkinInfoDb.setPcrTest(pax.get(15).asText());
                checkinInfoDb.setError(jsonNode.path("error_code").asText());

                checkinInfoRepository.save(checkinInfoDb);
            }
        }

        return checkinInfoRepository.findByBookCode(bookingCode).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CheckinInfoDto> print(PrintVo vo) {
        var data = UtilFn.getDataFromToken();
        var bookingCode = vo.getBookingCode();
        List<CheckinInfo> checkinInfos = new ArrayList<>();

        for (var ticket : vo.getTicketNumbers()) {
            var checkinInfo = checkinInfoRepository.findByTicketNumber(ticket)
                    .orElseThrow(() -> new CustomException(String.format(MAXIMUM_TIME, bookingCode), HttpStatus.BAD_REQUEST));

            String url = String.format("%s%s?rqid=%s&user_id=%s&token=%s&ticket_no=%s",
                    baseUrl, PelniMethod.PRINT.getUrl(), rqId, Objects.requireNonNull(data).getAnotherData().getUserId(),
                    data.getAnotherData().getData(), ticket);

            log.info("Request Print URL: {} with method: {}", url, PelniMethod.PRINT.getHttpMethod());

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url, PelniMethod.PRINT.getHttpMethod(), null, String.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("Request Print not 200: {}", responseEntity.getBody());
                throw new CustomException(responseEntity.getBody(), (HttpStatus) responseEntity.getStatusCode());
            }

            String response = responseEntity.getBody();
            log.info("Response Print: {}", response);

            UtilFn.saveLog(
                    data.getUsername(), data.getRole().toUpperCase(), String.format(FLAGING_CHECKIN, bookingCode, ticket),
                    url, response, data.getType().toUpperCase(), logNewRepository
            );

            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(response);
            } catch (JsonProcessingException e) {
                log.error("error parse json print: {}", e.getMessage());
                throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

            if (!"0".equals(jsonNode.path("error_code").asText())) {
                String errorMessage = jsonNode.path("error_message").asText();
                log.warn("Print failed: {}", errorMessage);
            } else {
                checkinInfos.add(checkinInfo);
            }
        }
        return checkinInfos.stream().map(this::toDto).collect(Collectors.toList());
    }

    private CheckinInfoDto toDto(CheckinInfo checkinInfo) {
        var bean = new CheckinInfoDto();
        bean.setId(checkinInfo.getId());
        bean.setNumberCode(checkinInfo.getNumberCode());
        bean.setBookingCode(checkinInfo.getBookingCode());
        bean.setShipNumber(checkinInfo.getShipNumber());
        bean.setShipName(checkinInfo.getShipName());
        bean.setAge(checkinInfo.getAge());
        bean.setYear(checkinInfo.getYear());
        bean.setOrganizationCode(checkinInfo.getOrganizationCode());
        bean.setOrganizationName(checkinInfo.getOrganizationName());
        bean.setOrganizationCall(checkinInfo.getOrganizationCall());
        bean.setDestinationCode(checkinInfo.getDestinationCode());
        bean.setDestinationName(checkinInfo.getDestinationName());
        bean.setDestinationCall(checkinInfo.getDestinationCall());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy/HH:mm");
        bean.setArrivalDateTime(LocalDateTime.of(checkinInfo.getArrivalDate(),
                LocalTime.parse(checkinInfo.getArrivalTime(), DateTimeFormatter.ofPattern("HHmm"))).format(formatter));
        bean.setDepartureDateTime(LocalDateTime.of(checkinInfo.getDepartureDate(),
                LocalTime.parse(checkinInfo.getDepartureTime(), DateTimeFormatter.ofPattern("HHmm"))).format(formatter));

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        bean.setTransactionDate(LocalDateTime.parse(checkinInfo.getTransactionDate(), dateTimeFormatter)
                .toInstant(ZoneOffset.UTC).toEpochMilli());
        bean.setMinCheckin(LocalDateTime.parse(checkinInfo.getMinCheckin(), dateTimeFormatter).format(formatter));
        bean.setMaxCheckin(LocalDateTime.parse(checkinInfo.getMaxCheckin(), dateTimeFormatter).format(formatter));

        bean.setSubclass(checkinInfo.getSubclass());
        bean.setMaxPrintTicket(checkinInfo.getMaxPrintTicket());
        bean.setClassType(checkinInfo.getClassType());
        bean.setFamily(checkinInfo.getFamily());
        bean.setStatus(checkinInfo.getStatus());
        bean.setLocation(checkinInfo.getLocation());
        bean.setName(checkinInfo.getName());
        bean.setIdentityNumber(checkinInfo.getIdentityNumber());
        bean.setAdult("A".equals(checkinInfo.getAdult()) ? ADULT : INFANT);
        bean.setDeck(checkinInfo.getDeck());
        bean.setCabin(checkinInfo.getCabin());
        bean.setBed(checkinInfo.getBed());
        bean.setGender("M".equals(checkinInfo.getGender()) ? MALE : FEMALE);
        bean.setTicketNumber(checkinInfo.getTicketNumber());
        bean.setPrintStatus(checkinInfo.getPrintStatus());
        bean.setTicketPrinted(checkinInfo.getTicketPrinted());
        bean.setPrice(checkinInfo.getPrice());
        bean.setAdminFee(checkinInfo.getAdminFee());
        bean.setNetPrice(checkinInfo.getNetPrice());
        bean.setVaccine(checkinInfo.getVaccine());
        bean.setAntigenTest(checkinInfo.getAntigenTest());
        bean.setPcrTest(checkinInfo.getPcrTest());

        return bean;
    }
}