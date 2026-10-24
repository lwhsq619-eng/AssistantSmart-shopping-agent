package com.example.mall.agent.controller;

import com.example.mall.agent.agent.AfterSaleTicketAgent;
import com.example.mall.agent.dto.agent.AfterSaleTicketRequest;
import com.example.mall.agent.dto.agent.AfterSaleTicketResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 售后工单智能分拣接口。
 * Controller 只调用 Agent，不直接调用 DeepSeek，不创建真实售后单。
 */
@RestController
@RequestMapping("/api/ai/after-sale")
public class AfterSaleTicketController {
    private final AfterSaleTicketAgent afterSaleTicketAgent;

    public AfterSaleTicketController(AfterSaleTicketAgent afterSaleTicketAgent) {
        this.afterSaleTicketAgent = afterSaleTicketAgent;
    }

    @PostMapping("/classify")
    public AfterSaleTicketResponse classify(@RequestBody AfterSaleTicketRequest request) {
        return afterSaleTicketAgent.classify(request);
    }
}
