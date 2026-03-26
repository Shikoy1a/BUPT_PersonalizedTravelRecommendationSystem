package com.travel.service;

import com.travel.common.PageData;
import com.travel.model.entity.ScenicArea;
import com.travel.model.vo.recommendation.ScenicAreaRecommendVO;

import java.util.List;

/**
 * 景区推荐服务。
 */
public interface RecommendationService
{

    /**
     * 获取推荐景区列表（基础：按 sortBy 排序）。
     */
    PageData<ScenicArea> list(Integer page, Integer size, String sortBy, String type);

    /**
     * 热门景区（按 heat / rating 综合排序的基础实现）。
     */
    PageData<ScenicArea> hot(Integer page, Integer size, String type);

    /**
     * 个性化推荐（基于用户兴趣权重与景区标签权重匹配）。
     */
    PageData<ScenicAreaRecommendVO> personalized(Long userId, Integer page, Integer size, String type, String tagKeyword);

    /**
     * 景区详情。
     */
    ScenicArea detail(Long id);

    /**
     * 按名称关键字筛选景区（内存匹配，用于前端目的地下拉）。
     */
    List<ScenicArea> searchScenicByKeyword(String keyword, int limit);
}

