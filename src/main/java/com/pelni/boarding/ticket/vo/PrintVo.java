package com.pelni.boarding.ticket.vo;


import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrintVo {
    @NotEmpty(message = "bookingCode must not be empty")
    private String bookingCode;

    List<String> ticketNumbers;
}
