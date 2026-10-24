package com.example.mall.agent.controller;

import com.example.mall.agent.agent.CustomerServiceAgent;
import com.example.mall.agent.dto.agent.CustomerServiceRequest;
import com.example.mall.agent.dto.agent.CustomerServiceResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全天候智能客服接口。
 * Controller 只负责接收请求并调用 Agent，不直接调用 DeepSeek。
 */
@RestController
@RequestMapping("/api/ai/customer-service")
public class CustomerServiceController {
    private final CustomerServiceAgent customerServiceAgent;

    public CustomerServiceController(CustomerServiceAgent customerServiceAgent) {
        this.customerServiceAgent = customerServiceAgent;
    }

    @PostMapping("/chat")
    public CustomerServiceResponse chat(@RequestBody CustomerServiceRequest request) {
        return customerServiceAgent.chat(request);
    }
}
