package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.SysFlowCategory;

public interface ISysFlowCategoryService {

    public List<SysFlowCategory> selectCategoryList(SysFlowCategory category);

    public SysFlowCategory selectCategoryById(Long categoryId);

    public List<SysFlowCategory> selectCategoryAll();

    public int insertCategory(SysFlowCategory category);

    public int updateCategory(SysFlowCategory category);

    public int deleteCategoryByIds(Long[] categoryIds);

    public boolean checkCategoryCodeUnique(SysFlowCategory category);
}