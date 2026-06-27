package com.ruoyi.flowable.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.flowable.domain.SysFlowCategory;
import com.ruoyi.flowable.mapper.SysFlowCategoryMapper;
import com.ruoyi.flowable.service.ISysFlowCategoryService;

@Service
public class SysFlowCategoryServiceImpl implements ISysFlowCategoryService {

    @Autowired
    private SysFlowCategoryMapper categoryMapper;

    @Override
    public List<SysFlowCategory> selectCategoryList(SysFlowCategory category) {
        return categoryMapper.selectCategoryList(category);
    }

    @Override
    public SysFlowCategory selectCategoryById(Long categoryId) {
        return categoryMapper.selectCategoryById(categoryId);
    }

    @Override
    public List<SysFlowCategory> selectCategoryAll() {
        return categoryMapper.selectCategoryAll();
    }

    @Override
    public int insertCategory(SysFlowCategory category) {
        return categoryMapper.insertCategory(category);
    }

    @Override
    public int updateCategory(SysFlowCategory category) {
        return categoryMapper.updateCategory(category);
    }

    @Override
    public int deleteCategoryByIds(Long[] categoryIds) {
        return categoryMapper.deleteCategoryByIds(categoryIds);
    }

    @Override
    public boolean checkCategoryCodeUnique(SysFlowCategory category) {
        Long categoryId = category.getCategoryId() == null ? -1L : category.getCategoryId();
        SysFlowCategory info = categoryMapper.checkCategoryCodeUnique(category.getCategoryCode());
        if (info != null && !info.getCategoryId().equals(categoryId)) {
            return false;
        }
        return true;
    }
}