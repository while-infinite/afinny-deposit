package by.afinny.deposit.openfeign.userservice;

import by.afinny.deposit.dto.userservice.ClientByPhoneDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "USER-SERVICE/client")
public interface UserClient {

    @GetMapping
    ResponseEntity<ClientByPhoneDto> getClientByPhone(@RequestParam(name = "mobilePhone") String phoneNumber);
}