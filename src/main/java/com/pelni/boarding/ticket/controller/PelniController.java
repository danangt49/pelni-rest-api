package com.pelni.boarding.ticket.controller;

import com.pelni.boarding.ticket.config.GlobalApiResponse;
import com.pelni.boarding.ticket.service.PelniService;
import com.pelni.boarding.ticket.vo.LoginVo;
import com.pelni.boarding.ticket.vo.PrintVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*", maxAge = 3600)
public class PelniController {

    private final PelniService pelniService;

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginVo vo) {
        return new GlobalApiResponse<>(pelniService.login(vo), HttpStatus.OK);
    }

    @GetMapping("check/{bookingCode}")
    public ResponseEntity<?> check(@PathVariable("bookingCode") String bookingCode) {
        return new GlobalApiResponse<>(pelniService.check(bookingCode), HttpStatus.OK);
    }

    @PostMapping("print")
    public ResponseEntity<?> print(@RequestBody @Valid PrintVo vo) {
        return new GlobalApiResponse<>(pelniService.print(vo), HttpStatus.OK);
    }
}