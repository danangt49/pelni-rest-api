package com.pelni.boarding.ticket.repository;

import com.pelni.boarding.ticket.entity.CheckinInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CheckinInfoRepository extends JpaRepository<CheckinInfo, Integer> {
    @Query(value = "SELECT ci FROM CheckinInfo AS ci WHERE ci.bookingCode = ?1 ORDER BY ci.id DESC")
    List<CheckinInfo> findByBookCode(String bookingCode);

    @Query(value = "SELECT ci FROM CheckinInfo AS ci WHERE ci.ticketNumber = ?1 AND ci.ticketPrinted < ci.maxPrintTicket " +
            "AND date_format(ci.departureDate, '%Y%m%d%H%i') >= ci.minCheckin ORDER BY ci.id DESC")
    Optional<CheckinInfo> findByTicketNumber(String ticket);
}