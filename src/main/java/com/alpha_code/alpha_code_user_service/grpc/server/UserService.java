package com.alpha_code.alpha_code_user_service.grpc.server;

import com.alpha_code.alpha_code_user_service.enums.AccountEnum;
import com.alpha_code.alpha_code_user_service.service.AccountService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import user.User;
import user.UserServiceGrpc;
import user.User.AccountInformation;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserService extends UserServiceGrpc.UserServiceImplBase {
    private final AccountService accountService;

    @PostConstruct
    public void init() {
        log.info("UserServiceGrpc initialized successfully");
    }

    @Override
    public void getAccount(User.GetAccountRequest request, StreamObserver<User.AccountInformation> responseObserver) {
        String requestId = request.getAccountId();
        log.info("Received GetAccount request for accountId={}", requestId);

        try {
            // Convert String -> UUID
            UUID accountId = UUID.fromString(requestId);

            // Lấy dữ liệu account từ DB/service
            var account = accountService.getById(accountId);
            if (account == null) {
                log.warn("Account not found for accountId={}", requestId);
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Account not found")
                        .asRuntimeException());
                return;
            }

            // Build response
            AccountInformation accountInformation = AccountInformation.newBuilder()
                    .setAccountId(account.getId().toString())
                    .setEmail(account.getEmail())
                    .setFullName(account.getFullName())
                    .setPhone(account.getPhone())
                    .setImage(account.getImage())
                    .setGender(AccountEnum.fromCode(account.getGender()))
                    .build();

            // Log chi tiết trước khi gửi
            log.info("Sending AccountInformation response for accountId={}, email={}, fullName={}",
                    accountInformation.getAccountId(),
                    accountInformation.getEmail(),
                    accountInformation.getFullName());

            // Gửi dữ liệu cho client
            responseObserver.onNext(accountInformation);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            // UUID không hợp lệ
            log.error("Invalid accountId format: {}", requestId, e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid accountId format")
                    .withCause(e)
                    .asRuntimeException());
        } catch (Exception e) {
            // Lỗi server khác
            log.error("Internal error while getting account for accountId={}", requestId, e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

}
