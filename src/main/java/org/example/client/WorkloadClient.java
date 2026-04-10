package org.example.client;

import org.example.dto.request.TrainerWorkloadRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("http://localhost:9090/api/workload")
public interface WorkloadClient {

    @PostExchange
    ResponseEntity<Void> processWorkload(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-Transaction-id") String transactionId,
            @RequestBody TrainerWorkloadRequest request);

}
