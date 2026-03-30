package com.travel.service.impl;

import com.travel.model.entity.Tag;
import com.travel.model.vo.TagVO;
import com.travel.service.TagService;
import com.travel.storage.InMemoryStore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务：从 {@link InMemoryStore} 读取 tags 镜像。
 */
@Service
public class TagServiceImpl implements TagService
{

    private final InMemoryStore store;

    public TagServiceImpl(InMemoryStore store)
    {
        this.store = store;
    }

    /** 不在下拉/兴趣选择中展示的标签名（与景区类型「普通景区」等区分） */
    private static final String EXCLUDED_TAG_NAME = "普通景区";

    @Override
    public List<TagVO> listAll()
    {
        return store.listAllTagsSortedByName().stream()
            .filter(t -> !isExcludedTag(t.getName()))
            .map(this::toVo)
            .collect(Collectors.toList());
    }

    private static boolean isExcludedTag(String name)
    {
        return StringUtils.isNotBlank(name) && EXCLUDED_TAG_NAME.equals(name.trim());
    }

    private TagVO toVo(Tag tag)
    {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setType(tag.getType());
        return vo;
    }
}
