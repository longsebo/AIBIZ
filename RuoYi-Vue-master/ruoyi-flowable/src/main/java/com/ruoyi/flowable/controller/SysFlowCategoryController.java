package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.flowable.domain.SysFlowCategory;
import com.ruoyi.flowable.service.ISysFlowCategoryService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/flowable/category")
public class SysFlowCategoryController extends BaseController {

    @Autowired
    private ISysFlowCategoryService categoryService;

    @PreAuthorize("@ss.hasPermi('flowable:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysFlowCategory category) {
        startPage();
        List<SysFlowCategory> list = categoryService.selectCategoryList(category);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('flowable:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId) {
        return success(categoryService.selectCategoryById(categoryId));
    }

    @GetMapping("/listAll")
    public AjaxResult listAll() {
        List<SysFlowCategory> list = categoryService.selectCategoryAll();
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('flowable:category:add')")
    @Log(title = "流程分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysFlowCategory category) {
        if (!categoryService.checkCategoryCodeUnique(category)) {
            return error("新增分类'" + category.getCategoryName() + "'失败，分类编码已存在");
        }
        return toAjax(categoryService.insertCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('flowable:category:edit')")
    @Log(title = "流程分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysFlowCategory category) {
        if (!categoryService.checkCategoryCodeUnique(category)) {
            return error("修改分类'" + category.getCategoryName() + "'失败，分类编码已存在");
        }
        return toAjax(categoryService.updateCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('flowable:category:remove')")
    @Log(title = "流程分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds) {
        return toAjax(categoryService.deleteCategoryByIds(categoryIds));
    }

    @PreAuthorize("@ss.hasPermi('flowable:category:export')")
    @Log(title = "流程分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysFlowCategory category) {
        List<SysFlowCategory> list = categoryService.selectCategoryList(category);
        ExcelUtil<SysFlowCategory> util = new ExcelUtil<SysFlowCategory>(SysFlowCategory.class);
        util.exportExcel(response, list, "流程分类数据");
    }
}