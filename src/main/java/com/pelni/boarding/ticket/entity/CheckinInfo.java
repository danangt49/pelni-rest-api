package com.pelni.boarding.ticket.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "checkin_info")
public class CheckinInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "num_code")
    private String numberCode;

    @Column(name = "book_code")
    private String bookingCode;

    @Column(name = "ship_no")
    private String shipNumber;

    @Column(name = "ship_name")
    private String shipName;

    @Column(name = "voyage_no")
    private String age;

    @Column(name = "year")
    private String year;

    @Column(name = "org")
    private String organizationCode;

    @Column(name = "org_name")
    private String organizationName;

    @Column(name = "org_call")
    private String organizationCall;

    @Column(name = "des")
    private String destinationCode;

    @Column(name = "des_name")
    private String destinationName;

    @Column(name = "des_call")
    private String destinationCall;

    @Column(name = "dep_date")
    private LocalDate departureDate;

    @Column(name = "dep_time")
    private String departureTime;

    @Column(name = "arv_date")
    private LocalDate arrivalDate;

    @Column(name = "arv_time")
    private String arrivalTime;

    @Column(name = "subclass")
    private String subclass;

    @Column(name = "max_print_ticket")
    private Integer maxPrintTicket;

    @Column(name = "max_checkin")
    private String maxCheckin;

    @Column(name = "min_checkin")
    private String minCheckin;

    @Column(name = "class")
    private String classType;

    @Column(name = "family")
    private String family;

    @Column(name = "status")
    private String status;

    @Column(name = "transaction_date")
    private String transactionDate;

    @Column(name = "location")
    private String location;

    @Column(name = "nama")
    private String name;

    @Column(name = "no_identitas")
    private String identityNumber;

    @Column(name = "adult_child_infant")
    private String adult;

    @Column(name = "deck")
    private String deck;

    @Column(name = "no_cabin")
    private String cabin;

    @Column(name = "no_bed")
    private String bed;

    @Column(name = "gender")
    private String gender;

    @Column(name = "no_ticket")
    private String ticketNumber;

    @Column(name = "print_status")
    private String printStatus;

    @Column(name = "jumlah_ticket_tercetak")
    private Integer ticketPrinted;

    @Column(name = "tarif")
    private String price;

    @Column(name = "tarif_admin")
    private String adminFee;

    @Column(name = "tarif_net")
    private String netPrice;

    @Column(name = "vaksin")
    private String vaccine;

    @Column(name = "antigen")
    private String antigenTest;

    @Column(name = "pcr")
    private String pcrTest;

    @Column(name = "errorpl", columnDefinition = "VARCHAR(254) DEFAULT ''")
    private String error;
}
