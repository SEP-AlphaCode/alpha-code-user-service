package com.alpha_code.alpha_code_user_service.grpc.client;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import payment.GetKeyResponse;
import payment.PaymentServiceGrpc;

import java.util.UUID;

@Service
@Slf4j
public class PaymentServiceClient {
    @GrpcClient("alpha-payment-service")
    private PaymentServiceGrpc.PaymentServiceBlockingStub blockingStub;

    public GetKeyResponse getKeyByAccountId(UUID accountId) {
        try {
            payment.GetRequest request = payment.GetRequest.newBuilder()
                    .setId(accountId.toString())
                    .build();
            return blockingStub.getKeyByAccountId(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC call to getAccountKey failed: {}", e.getStatus());
            throw e;
        }
    }
}
