package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.service.DashboardService;
import com.example.mall.vo.DashboardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public Result<DashboardVO> dashboard() {
        return Result.success("查询成功", dashboardService.getDashboard());
    }
}
