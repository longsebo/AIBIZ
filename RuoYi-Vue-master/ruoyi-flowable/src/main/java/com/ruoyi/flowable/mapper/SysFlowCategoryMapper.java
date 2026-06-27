package com.ruoyi.flowable.mapper;

import java.util.List;
import com.ruoyi.flowable.domain.SysFlowCategory;

public interface SysFlowCategoryMapper {

    public List<SysFlowCategory> selectCategoryList(SysFlowCategory category);

    public SysFlowCategory selectCategoryById(Long categoryId);

    public List<SysFlowCategory> selectCategoryAll();

    public int insertCategory(SysFlowCategory category);

    public int updateCategory(SysFlowCategory category);

    public int deleteCategoryById(Long categoryId);

    public int deleteCategoryByIds(Long[] categoryIds);

    public SysFlowCategory checkCategoryCodeUnique(String categoryCode);
}