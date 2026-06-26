package com.ruoyi.flowable.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.flowable.domain.SysProcessForm;
import com.ruoyi.flowable.service.ISysProcessFormService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flowable/processForm")
public class SysProcessFormController extends BaseController {

    @Autowired
    private ISysProcessFormService processFormService;

    @PreAuthorize("@ss.hasPermi('flowable:processForm:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysProcessForm processForm) {
        startPage();
        List<SysProcessForm> list = processFormService.selectProcessFormList(processForm);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('flowable:processForm:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(processFormService.selectProcessFormById(id));
    }

    @GetMapping("/process/{processKey}")
    public AjaxResult getByProcessKey(@PathVariable String processKey) {
        return success(processFormService.selectProcessFormByProcessKey(processKey));
    }

    @PreAuthorize("@ss.hasPermi('flowable:processForm:add')")
    @Log(title = "流程表单关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysProcessForm processForm) {
        return toAjax(processFormService.insertProcessForm(processForm));
    }

    @PreAuthorize("@ss.hasPermi('flowable:processForm:edit')")
    @Log(title = "流程表单关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysProcessForm processForm) {
        return toAjax(processFormService.updateProcessForm(processForm));
    }

    @PreAuthorize("@ss.hasPermi('flowable:processForm:remove')")
    @Log(title = "流程表单关联", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(processFormService.deleteProcessFormByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('flowable:processForm:export')")
    @Log(title = "流程表单关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysProcessForm processForm) {
        List<SysProcessForm> list = processFormService.selectProcessFormList(processForm);
        ExcelUtil<SysProcessForm> util = new ExcelUtil<SysProcessForm>(SysProcessForm.class);
        util.exportExcel(response, list, "流程表单关联数据");
    }
}
