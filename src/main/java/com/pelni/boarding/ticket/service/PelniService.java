package com.pelni.boarding.ticket.service;

import com.pelni.boarding.ticket.dto.CheckinInfoDto;
import com.pelni.boarding.ticket.dto.LoginDto;
import com.pelni.boarding.ticket.vo.LoginVo;
import com.pelni.boarding.ticket.vo.PrintVo;

import java.util.List;

public interface PelniService {
    LoginDto login(LoginVo vo);
    List<CheckinInfoDto> check(String bookingCode);
    List<CheckinInfoDto> print(PrintVo vo);
}
