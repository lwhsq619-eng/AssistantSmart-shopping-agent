package com.example.mall.agent.controller;

import com.example.mall.agent.agent.MerchantCopywritingAgent;
import com.example.mall.agent.dto.agent.CopywritingRequest;
import com.example.mall.agent.dto.agent.CopywritingResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家运营商品文案生成接口。
 * Controller 只调用 Agent，不直接调用 ProductTool 或 DeepSeekService。
 */
@RestController
@RequestMapping("/api/ai/merchant/copywriting")
public class MerchantCopywritingController {
    private final MerchantCopywritingAgent merchantCopywritingAgent;

    public MerchantCopywritingController(MerchantCopywritingAgent merchantCopywritingAgent) {
        this.merchantCopywritingAgent = merchantCopywritingAgent;
    }

    @PostMapping("/generate")
    public CopywritingResponse generate(@Valid @RequestBody CopywritingRequest request) {
        return merchantCopywritingAgent.generate(request);
    }
}
