package com.example.onionstore.domain.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OrderSearchRequest {
    private String keyword;
    private LocalDate startDate;
    private LocalDate endDate;
}
