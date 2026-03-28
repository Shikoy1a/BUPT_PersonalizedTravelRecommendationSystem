package com.travel.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * 单个兴趣项的更新请求。
 */
public class InterestItemRequest
{

    @NotBlank(message = "兴趣类型不能为空")
    private String type;

    @DecimalMin(value = "0.0", inclusive = false, message = "兴趣权重必须大于0")
    @DecimalMax(value = "5.0", message = "兴趣权重不能大于5")
    private Double weight;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type == null ? null : type.trim();
    }

    public Double getWeight()
    {
        return weight;
    }

    public void setWeight(Double weight)
    {
        this.weight = weight;
    }
}

