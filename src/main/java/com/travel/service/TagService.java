package com.travel.service;

import com.travel.model.vo.TagVO;

import java.util.List;

/**
 * 标签查询（数据来自启动时预加载的内存镜像，对应 {@code tags} 表）。
 */
public interface TagService
{

    /**
     * 全部标签，按名称排序。
     */
    List<TagVO> listAll();
}
