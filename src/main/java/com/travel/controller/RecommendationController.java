package com.travel.controller;

import com.travel.common.ApiResponse;
import com.travel.common.PageData;
import com.travel.model.entity.ScenicArea;
import com.travel.model.vo.recommendation.ScenicAreaRecommendVO;
import com.travel.security.SecurityUtil;
import com.travel.service.RecommendationService;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 推荐相关接口。
 */
@RestController
@RequestMapping("/api/recommendation")
public class RecommendationController
{

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService)
    {
        this.recommendationService = recommendationService;
    }

    /**
     * 获取推荐景区列表。
     */
    @GetMapping
    public ApiResponse<PageData<ScenicArea>> list(@RequestParam(value = "page", required = false) Integer page,
                                                  @RequestParam(value = "size", required = false) Integer size,
                                                  @RequestParam(value = "sortBy", required = false) String sortBy,
                                                  @RequestParam(value = "type", required = false) String type)
    {
        return ApiResponse.success(recommendationService.list(page, size, sortBy, type), "获取成功");
    }

    /**
     * 个性化推荐。
     */
    @GetMapping("/personalized")
    public ApiResponse<PageData<ScenicAreaRecommendVO>> personalized(@RequestParam(value = "page", required = false) Integer page,
                                                                     @RequestParam(value = "size", required = false) Integer size,
                                                                     @RequestParam(value = "type", required = false) String type,
                                                                     @RequestParam(value = "tagKeyword", required = false) String tagKeyword)
    {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null)
        {
            return ApiResponse.failure(401, "未登录或令牌无效");
        }
        return ApiResponse.success(recommendationService.personalized(userId, page, size, type, tagKeyword), "获取成功");
    }

    /**
     * 热门景区。
     */
    @GetMapping("/hot")
    public ApiResponse<PageData<ScenicArea>> hot(@RequestParam(value = "page", required = false) Integer page,
                                                 @RequestParam(value = "size", required = false) Integer size,
                                                 @RequestParam(value = "type", required = false) String type)
    {
        return ApiResponse.success(recommendationService.hot(page, size, type), "获取成功");
    }

    /**
     * 景区详情。
     */
    @GetMapping("/detail/{id}")
    public ApiResponse<ScenicArea> detail(@PathVariable("id") @NotNull Long id)
    {
        return ApiResponse.success(recommendationService.detail(id), "获取成功");
    }

    /**
     * 按名称关键字筛选景区（内存匹配，供前端目的地下拉）。
     */
    @GetMapping("/scenic-search")
    public ApiResponse<List<ScenicArea>> scenicSearch(@RequestParam("keyword") String keyword,
                                                       @RequestParam(value = "limit", required = false) Integer limit)
    {
        int lim = limit == null ? 50 : limit;
        return ApiResponse.success(recommendationService.searchScenicByKeyword(keyword, lim), "获取成功");
    }
}

