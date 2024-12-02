package com.pelni.boarding.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnotherDataDto {
    private String userId;
    private String firstName;
    private String phoneNumber;
    private String email;
    private String office;
    private String branch;
    private String data;
}
