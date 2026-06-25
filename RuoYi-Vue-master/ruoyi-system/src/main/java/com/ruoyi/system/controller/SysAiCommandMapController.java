package com.ruoyi.system.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AiCommandMap;
import com.ruoyi.system.service.IAiCommandMapService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI指令匹配Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/aiCommand")
public class SysAiCommandMapController extends BaseController {

    @Autowired
    private IAiCommandMapService aiCommandMapService;

    /**
     * 获取所有指令映射（供AI前端使用）
     */
    @GetMapping("/listAll")
    public AjaxResult listAll() {
        List<AiCommandMap> list = aiCommandMapService.selectAiCommandMapAll();
        return success(list);
    }

    /**
     * 根据指令关键词查询
     */
    @GetMapping("/{command}")
    public AjaxResult getByCommand(@PathVariable String command) {
        AiCommandMap map = aiCommandMapService.selectAiCommandMapByCommand(command);
        return success(map);
    }

    /**
     * AI语义匹配指令
     * @param userInput 用户输入的指令
     * @return 匹配到的指令映射
     */
    @PostMapping("/aiMatch")
    public AjaxResult aiMatch(@RequestBody String userInput) {
        List<AiCommandMap> list = aiCommandMapService.selectAiCommandMapAll();
        AiCommandMap result = aiCommandMapService.aiMatchCommand(userInput, list);
        if (result != null) {
            return success(result);
        }
        return error("无法识别的指令");
    }

    // ... 以下是原有的管理接口 ...

    /**
     * 查询指令映射列表
     */
    @PreAuthorize("@ss.hasPermi('system:aiCommand:list')")
    @GetMapping("/list")
    public com.ruoyi.common.core.page.TableDataInfo list(AiCommandMap aiCommandMap) {
        startPage();
        List<AiCommandMap> list = aiCommandMapService.selectAiCommandMapList(aiCommandMap);
        return getDataTable(list);
    }

    /**
     * 导出指令映射列表
     */
    @PreAuthorize("@ss.hasPermi('system:aiCommand:export')")
    @Log(title = "AI指令映射", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AiCommandMap aiCommandMap) {
        List<AiCommandMap> list = aiCommandMapService.selectAiCommandMapList(aiCommandMap);
        ExcelUtil<AiCommandMap> util = new ExcelUtil<AiCommandMap>(AiCommandMap.class);
        util.exportExcel(response, list, "AI指令映射数据");
    }

    /**
     * 获取指令映射详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:aiCommand:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(aiCommandMapService.selectAiCommandMapById(id));
    }

    /**
     * 新增指令映射
     */
    @PreAuthorize("@ss.hasPermi('system:aiCommand:add')")
    @Log(title = "AI指令映射", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AiCommandMap aiCommandMap) {
        return toAjax(aiCommandMapService.insertAiCommandMap(aiCommandMap));
    }

    /**
     * 修改指令映射
     */
    @PreAuthorize("@ss.hasPermi('system:aiCommand:edit')")
    @Log(title = "AI指令映射", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AiCommandMap aiCommandMap) {
        return toAjax(aiCommandMapService.updateAiCommandMap(aiCommandMap));
    }

    /**
     * 删除指令映射
     */
    @PreAuthorize("@ss.hasPermi('system:aiCommand:remove')")
    @Log(title = "AI指令映射", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(aiCommandMapService.deleteAiCommandMapById(ids[0]));
    }
}
