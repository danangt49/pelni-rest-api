package com.pelni.boarding.ticket.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "flag_alt")
public class FlagAlt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "notiket")
    private String ticketNumber;

    @Column(name = "flag")
    private String flag;
}
