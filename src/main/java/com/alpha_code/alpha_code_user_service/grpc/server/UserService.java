package com.alpha_code.alpha_code_user_service.grpc.server;

import com.alpha_code.alpha_code_user_service.enums.AccountEnum;
import com.alpha_code.alpha_code_user_service.service.AccountService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import user.User;
import user.UserServiceGrpc;
import user.User.AccountInformation;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService extends UserServiceGrpc.UserServiceImplBase {
    private final AccountService accountService;

    @Override
    public void getAccount(User.GetAccountRequest request, StreamObserver<User.AccountInformation> responseObserver) {
        try {
            UUID accountId = UUID.fromString(request.getAccountId());
            var account = accountService.getById(accountId);

            AccountInformation accountInformation = AccountInformation.newBuilder()
                    .setAccountId(account.getId().toString())
                    .setEmail(account.getEmail())
                    .setFullName(account.getFullName())
                    .setPhone(account.getPhone())
                    .setImage(account.getImage())
                    .setGender(AccountEnum.fromCode(account.getGender()))
                    .build();

            // gửi response
            responseObserver.onNext(accountInformation);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // gửi lỗi nếu có
            responseObserver.onError(e);
        }
    }
}
