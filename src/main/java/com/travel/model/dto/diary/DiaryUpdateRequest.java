package com.travel.model.dto.diary;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 更新日记请求。
 */
public class DiaryUpdateRequest
{

    /** 由路径参数注入，请求体不必传 */
    private Long id;

    @NotBlank(message = "title 不能为空")
    private String title;

    @NotBlank(message = "content 不能为空")
    private String content;

    private List<String> images;

    private List<String> videos;

    /**
     * 目的地（可选；编辑时前端会传）。
     * 若不传则保持原有关联不变。
     */
    private List<Long> destinations;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public List<String> getImages()
    {
        return images;
    }

    public void setImages(List<String> images)
    {
        this.images = images;
    }

    public List<String> getVideos()
    {
        return videos;
    }

    public void setVideos(List<String> videos)
    {
        this.videos = videos;
    }

    public List<Long> getDestinations()
    {
        return destinations;
    }

    public void setDestinations(List<Long> destinations)
    {
        this.destinations = destinations;
    }
}

