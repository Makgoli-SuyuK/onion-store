package com.example.onionstore.domain.refund.repository;

import com.example.onionstore.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RefundRepository extends JpaRepository<Refund, Long>, RefundCustomRepository {

}
