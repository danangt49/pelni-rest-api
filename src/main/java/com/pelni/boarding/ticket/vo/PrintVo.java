package com.pelni.boarding.ticket.vo;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrintVo {
    @NotNull
    @NotEmpty
    private String bookingCode;

    List<String> ticketNumbers;
}
