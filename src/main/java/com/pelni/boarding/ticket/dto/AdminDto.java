package com.pelni.boarding.ticket.dto;

import com.pelni.boarding.ticket.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link Admin}
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDto implements Serializable {
    private String username;
    private String name;
    private String role;
    private String type;
    private String printer;
    private AnotherDataDto anotherData;
}