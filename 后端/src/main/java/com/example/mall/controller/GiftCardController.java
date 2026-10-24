package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.dto.GiftCardPurchaseDTO;
import com.example.mall.entity.GiftCard;
import com.example.mall.entity.UserGiftCard;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.GiftCardMapper;
import com.example.mall.mapper.UserGiftCardMapper;
import com.example.mall.utils.AuthContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gift-cards")
public class GiftCardController {
    private final GiftCardMapper giftCardMapper;
    private final UserGiftCardMapper userGiftCardMapper;

    public GiftCardController(GiftCardMapper giftCardMapper, UserGiftCardMapper userGiftCardMapper) {
        this.giftCardMapper = giftCardMapper;
        this.userGiftCardMapper = userGiftCardMapper;
    }

    @GetMapping
    public Result<List<GiftCard>> list() {
        return Result.success("查询成功", giftCardMapper.selectList(new LambdaQueryWrapper<GiftCard>()
                .eq(GiftCard::getStatus, 1)
                .orderByAsc(GiftCard::getAmount)));
    }

    @PostMapping("/purchase")
    public Result<UserGiftCard> purchase(@Valid @RequestBody GiftCardPurchaseDTO dto) {
        GiftCard card = giftCardMapper.selectById(dto.getGiftCardId());
        if (card == null || card.getStatus() == null || card.getStatus() == 0) {
            throw new BusinessException("礼品卡不存在");
        }
        UserGiftCard userCard = new UserGiftCard();
        userCard.setUserId(AuthContext.userId());
        userCard.setGiftCardId(card.getId());
        userCard.setGiftCardName(card.getName());
        userCard.setAmount(card.getAmount());
        userCard.setRecipient(dto.getRecipient());
        userCard.setBlessing(dto.getBlessing());
        userCard.setRedeemCode("GC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        userCard.setStatus("未使用");
        userCard.setCreateTime(LocalDateTime.now());
        userGiftCardMapper.insert(userCard);
        return Result.success("购买成功", userCard);
    }

    @GetMapping("/my")
    public Result<List<UserGiftCard>> my() {
        return Result.success("查询成功", userGiftCardMapper.selectList(new LambdaQueryWrapper<UserGiftCard>()
                .eq(UserGiftCard::getUserId, AuthContext.userId())
                .orderByDesc(UserGiftCard::getCreateTime)));
    }

    @PostMapping("/redeem")
    public Result<Void> redeem(@RequestParam String code) {
        UserGiftCard card = userGiftCardMapper.selectOne(new LambdaQueryWrapper<UserGiftCard>()
                .eq(UserGiftCard::getUserId, AuthContext.userId())
                .eq(UserGiftCard::getRedeemCode, code)
                .last("limit 1"));
        if (card == null) {
            throw new BusinessException("兑换码不存在");
        }
        if ("已使用".equals(card.getStatus())) {
            throw new BusinessException("兑换码已使用");
        }
        card.setStatus("已使用");
        userGiftCardMapper.updateById(card);
        return Result.success();
    }
}
