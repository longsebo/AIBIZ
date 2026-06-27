package com.ruoyi.flowable.domain;

import java.io.Serializable;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class SysFlowCategory extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long categoryId;

    @Excel(name = "分类名称")
    private String categoryName;

    @Excel(name = "分类编码")
    private String categoryCode;

    private Long parentId;

    @Excel(name = "显示顺序")
    private Integer orderNum;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}