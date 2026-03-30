package com.travel.model.vo;

import java.io.Serial;
import java.io.Serializable;

/**
 * 标签视图，对应 {@code tags} 表行。
 */
public class TagVO implements Serializable
{

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String type;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
