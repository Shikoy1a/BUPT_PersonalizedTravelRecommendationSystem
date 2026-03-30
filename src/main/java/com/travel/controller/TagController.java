package com.travel.controller;

import com.travel.common.ApiResponse;
import com.travel.model.vo.TagVO;
import com.travel.service.TagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签列表（来源：{@code tags} 表经预加载后的内存数据）。
 */
@RestController
@RequestMapping("/api/tags")
public class TagController
{

    private final TagService tagService;

    public TagController(TagService tagService)
    {
        this.tagService = tagService;
    }

    @GetMapping
    public ApiResponse<List<TagVO>> list()
    {
        return ApiResponse.success(tagService.listAll(), "获取成功");
    }
}
