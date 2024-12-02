package com.pelni.boarding.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.pelni.boarding.ticket.entity.CheckinInfo}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckinInfoDto implements Serializable {
    private Integer id;
    private String numberCode;
    private String bookingCode;
    private String shipNumber;
    private String shipName;
    private String age;
    private String year;
    private String organizationCode;
    private String organizationName;
    private String organizationCall;
    private String destinationCode;
    private String destinationName;
    private String destinationCall;
    private String departureDateTime;
    private String arrivalDateTime;
    private String subclass;
    private Integer maxPrintTicket;
    private String maxCheckin;
    private String minCheckin;
    private String classType;
    private String family;
    private String status;
    private long transactionDate;
    private String location;
    private String name;
    private String identityNumber;
    private String adult;
    private String deck;
    private String cabin;
    private String bed;
    private String gender;
    private String ticketNumber;
    private String printStatus;
    private Integer ticketPrinted;
    private String price;
    private String adminFee;
    private String netPrice;
    private String vaccine;
    private String antigenTest;
    private String pcrTest;
    private String error;
}