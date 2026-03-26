package com.travel.service.impl;

import com.travel.common.PageData;
import com.travel.storage.InMemoryStore;
import com.travel.model.entity.ScenicArea;
import com.travel.model.vo.recommendation.ScenicAreaRecommendVO;
import com.travel.service.RecommendationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 推荐服务实现。
 *
 * <p>
 * 个性化推荐核心思想：
 * <ul>
 *     <li>用户兴趣：user_interests(interest_type, weight)</li>
 *     <li>景区标签：scenic_area_tags(tag_id, weight) + tags(name)</li>
 * </ul>
 * 通过标签名与兴趣类型做匹配（约定：标签名与兴趣类型一致或高度相关），计算匹配得分：
 * score = Σ(userWeight * tagWeight) + 0.2 * heatNorm + 0.2 * ratingNorm（基础融合策略，便于后续替换）
 * </p>
 */
@Service
public class RecommendationServiceImpl implements RecommendationService
{

    private final InMemoryStore store;

    public RecommendationServiceImpl(InMemoryStore store)
    {
        this.store = store;
    }

    @Override
    public PageData<ScenicArea> list(Integer page, Integer size, String sortBy, String type)
    {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size <= 0 ? 10 : Math.min(size, 50);
        int offset = (p - 1) * s;

        List<ScenicArea> all = StringUtils.isNotBlank(type) ? store.findScenicAreasByType(type) : store.findAllScenicAreas();
        for (ScenicArea scenic : all)
        {
            scenic.setTags(store.getScenicAreaTagNames(scenic.getId()));
        }

        all.sort((a, b) ->
        {
            if ("rating".equalsIgnoreCase(sortBy))
            {
                double ra = a.getRating() == null ? 0.0 : a.getRating();
                double rb = b.getRating() == null ? 0.0 : b.getRating();
                return Double.compare(rb, ra);
            }
            if ("heat".equalsIgnoreCase(sortBy))
            {
                int ha = a.getHeat() == null ? 0 : a.getHeat();
                int hb = b.getHeat() == null ? 0 : b.getHeat();
                return Integer.compare(hb, ha);
            }
            int ha = a.getHeat() == null ? 0 : a.getHeat();
            int hb = b.getHeat() == null ? 0 : b.getHeat();
            if (ha != hb)
            {
                return Integer.compare(hb, ha);
            }
            double ra = a.getRating() == null ? 0.0 : a.getRating();
            double rb = b.getRating() == null ? 0.0 : b.getRating();
            return Double.compare(rb, ra);
        });

        int total = all.size();
        if (offset >= total)
        {
            return new PageData<>(List.of(), (long) total);
        }
        int to = Math.min(offset + s, total);
        return new PageData<>(all.subList(offset, to), (long) total);
    }

    @Override
    public PageData<ScenicArea> hot(Integer page, Integer size, String type)
    {
        // 热门：优先热度，其次评分（基础实现）
        return list(page, size, "heat", type);
    }

    @Override
    public PageData<ScenicAreaRecommendVO> personalized(Long userId, Integer page, Integer size, String type, String tagKeyword)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("userId 不能为空");
        }
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size <= 0 ? 10 : Math.min(size, 50);
        // 兼容旧前端：很多页面仍把输入框绑定到 type。
        // 若 type 未匹配到任何景点类型且 tagKeyword 为空，则把 type 视作关键词。
        String effectiveTagKeyword = tagKeyword;
        List<ScenicArea> base = StringUtils.isNotBlank(type) ? store.findScenicAreasByType(type) : store.findAllScenicAreas();
        if (base.isEmpty() && StringUtils.isBlank(tagKeyword) && StringUtils.isNotBlank(type))
        {
            base = store.findAllScenicAreas();
            effectiveTagKeyword = type;
        }
        if (base.isEmpty())
        {
            return new PageData<>(List.of(), 0L);
        }

        String normalizedTagKeyword = normalize(effectiveTagKeyword);
        if (normalizedTagKeyword != null)
        {
            List<ScenicArea> filtered = new ArrayList<>();
            for (ScenicArea scenic : base)
            {
                if (containsTagKeyword(scenic.getId(), normalizedTagKeyword))
                {
                    filtered.add(scenic);
                }
            }
            base = filtered;
            if (base.isEmpty())
            {
                return new PageData<>(List.of(), 0L);
            }
        }

        // 候选集：取热度靠前的一批再做个性化计算，避免全量计算
        int candidateLimit = Math.min(300, p * s * 10);
        base.sort((a, b) ->
        {
            int ha = a.getHeat() == null ? 0 : a.getHeat();
            int hb = b.getHeat() == null ? 0 : b.getHeat();
            if (ha != hb)
            {
                return Integer.compare(hb, ha);
            }
            double ra = a.getRating() == null ? 0.0 : a.getRating();
            double rb = b.getRating() == null ? 0.0 : b.getRating();
            return Double.compare(rb, ra);
        });
        if (base.size() > candidateLimit)
        {
            base = base.subList(0, candidateLimit);
        }

        Map<String, Double> interestWeights = store.getUserInterests(userId);

        int maxHeat = 1;
        for (ScenicArea sa : base)
        {
            if (sa.getHeat() != null && sa.getHeat() > maxHeat)
            {
                maxHeat = sa.getHeat();
            }
        }

        List<ScenicAreaRecommendVO> scored = new ArrayList<>(base.size());
        for (ScenicArea scenic : base)
        {
            scenic.setTags(store.getScenicAreaTagNames(scenic.getId()));
            double matchScore = 0.0;
            Map<String, Double> tags = store.getScenicAreaTagWeights(scenic.getId());
            if (tags != null)
            {
                for (Map.Entry<String, Double> e : tags.entrySet())
                {
                    Double uw = interestWeights == null ? null : interestWeights.get(e.getKey());
                    if (uw != null)
                    {
                        matchScore += uw * (e.getValue() == null ? 1.0 : e.getValue());
                    }
                }
            }

            double heatNorm = (scenic.getHeat() == null ? 0.0 : scenic.getHeat()) / (double) maxHeat;
            double ratingNorm = Math.min(Math.max((scenic.getRating() == null ? 0.0 : scenic.getRating()) / 5.0, 0.0), 1.0);
            double score = matchScore + 0.2 * heatNorm + 0.2 * ratingNorm;

            ScenicAreaRecommendVO vo = new ScenicAreaRecommendVO();
            vo.setScenicArea(scenic);
            vo.setScore(score);
            scored.add(vo);
        }

        scored.sort((a, b) ->
        {
            double sa = a.getScore() == null ? 0.0 : a.getScore();
            double sb = b.getScore() == null ? 0.0 : b.getScore();
            return Double.compare(sb, sa);
        });

        int from = (p - 1) * s;
        if (from >= scored.size())
        {
            return new PageData<>(List.of(), (long) scored.size());
        }
        int to = Math.min(from + s, scored.size());
        return new PageData<>(scored.subList(from, to), (long) scored.size());
    }

    private boolean containsTagKeyword(Long scenicAreaId, String normalizedTagKeyword)
    {
        Map<String, Double> tags = store.getScenicAreaTagWeights(scenicAreaId);
        if (tags == null || tags.isEmpty())
        {
            return false;
        }
        for (String tagName : tags.keySet())
        {
            if (tagName == null)
            {
                continue;
            }
            String normalizedTagName = tagName.trim().toLowerCase();
            if (normalizedTagName.contains(normalizedTagKeyword))
            {
                return true;
            }
        }
        return false;
    }

    private String normalize(String s)
    {
        if (StringUtils.isBlank(s))
        {
            return null;
        }
        return s.trim().toLowerCase();
    }

    @Override
    public ScenicArea detail(Long id)
    {
        ScenicArea scenicArea = store.findScenicAreaById(id);
        if (scenicArea == null)
        {
            throw new IllegalArgumentException("景区不存在");
        }
        return scenicArea;
    }

    @Override
    public List<ScenicArea> searchScenicByKeyword(String keyword, int limit)
    {
        if (StringUtils.isBlank(keyword))
        {
            return List.of();
        }
        int lim = limit <= 0 ? 50 : Math.min(limit, 100);
        String q = keyword.trim().toLowerCase();
        List<ScenicArea> out = new ArrayList<>();
        for (ScenicArea sa : store.findAllScenicAreas())
        {
            String name = sa.getName();
            if (name != null && name.toLowerCase().contains(q))
            {
                out.add(sa);
                if (out.size() >= lim)
                {
                    break;
                }
            }
        }
        return out;
    }
}

