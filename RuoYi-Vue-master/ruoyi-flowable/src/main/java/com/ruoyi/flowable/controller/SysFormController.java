package com.ruoyi.flowable.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.flowable.domain.SysForm;
import com.ruoyi.flowable.service.ISysFormService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flowable/form")
public class SysFormController extends BaseController {

    @Autowired
    private ISysFormService formService;

    @PreAuthorize("@ss.hasPermi('flowable:form:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysForm form) {
        startPage();
        List<SysForm> list = formService.selectFormList(form);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('flowable:form:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(formService.selectFormById(id));
    }

    @PreAuthorize("@ss.hasPermi('flowable:form:add')")
    @Log(title = "表单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysForm form) {
        return toAjax(formService.insertForm(form));
    }

    @PreAuthorize("@ss.hasPermi('flowable:form:edit')")
    @Log(title = "表单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysForm form) {
        return toAjax(formService.updateForm(form));
    }

    @PreAuthorize("@ss.hasPermi('flowable:form:remove')")
    @Log(title = "表单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(formService.deleteFormByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('flowable:form:export')")
    @Log(title = "表单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysForm form) {
        List<SysForm> list = formService.selectFormList(form);
        ExcelUtil<SysForm> util = new ExcelUtil<SysForm>(SysForm.class);
        util.exportExcel(response, list, "表单数据");
    }
}
