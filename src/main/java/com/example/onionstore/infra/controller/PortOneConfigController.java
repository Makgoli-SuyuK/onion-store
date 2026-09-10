package com.example.onionstore.infra.controller;

import com.example.onionstore.global.dto.ApiResponse;
import com.example.onionstore.infra.config.PortOneProperties;
import com.example.onionstore.infra.dto.PortOneConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PortOneConfigController {

    private final PortOneProperties properties;

    @GetMapping("/api/config/portone")
    public ResponseEntity<ApiResponse<PortOneConfigResponse>> getConfig(){
        return ResponseEntity.ok(ApiResponse.success(new PortOneConfigResponse(
                properties.getStoreId(),
                properties.getChannelKey()
        )));
    }
}
