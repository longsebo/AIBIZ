package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.flowable.domain.SysProcessCc;
import com.ruoyi.flowable.service.ISysProcessCcService;

@RestController
@RequestMapping("/flowable/cc")
public class SysProcessCcController extends BaseController {

    @Autowired
    private ISysProcessCcService ccService;

    @GetMapping("/list")
    public AjaxResult list() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        List<SysProcessCc> list = ccService.selectCcByUserId(loginUser.getUserId());
        return success(list);
    }

    @PostMapping("/read")
    @Log(title = "流程抄送", businessType = BusinessType.UPDATE)
    public AjaxResult read(@RequestParam Long id) {
        ccService.updateCcStatus(id);
        return success();
    }
}