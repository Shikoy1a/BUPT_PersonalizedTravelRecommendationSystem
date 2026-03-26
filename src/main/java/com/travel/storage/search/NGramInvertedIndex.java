package com.travel.storage.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * N-Gram 倒排索引（简化版）。
 *
 * <p>
 * 把文本拆成长度为 n 的片段（n=2/3 默认），建立 term -> ids 映射。
 * 查询时对 query 生成 ngrams，统计命中次数作为相关度，再返回候选列表。
 * </p>
 */
public class NGramInvertedIndex
{

    private final int minN;
    private final int maxN;

    private final Map<String, List<Long>> inverted = new HashMap<>();

    public NGramInvertedIndex()
    {
        this(2, 3);
    }

    public NGramInvertedIndex(int minN, int maxN)
    {
        this.minN = Math.max(2, minN);
        this.maxN = Math.max(minN, maxN);
    }

    public void insert(String text, long id)
    {
        if (text == null)
        {
            return;
        }
        String t = normalize(text);
        if (t.length() < minN)
        {
            return;
        }

        for (int n = minN; n <= maxN; n++)
        {
            for (int i = 0; i + n <= t.length(); i++)
            {
                String term = t.substring(i, i + n);
                inverted.computeIfAbsent(term, k -> new ArrayList<>()).add(id);
            }
        }
    }

    /**
     * 查询候选 ids（按粗略相关度降序）。
     */
    public List<Long> search(String query, int limit)
    {
        if (query == null)
        {
            return List.of();
        }
        String q = normalize(query);
        if (q.length() < minN)
        {
            return List.of();
        }

        Map<Long, Integer> score = new HashMap<>();
        for (int n = minN; n <= maxN; n++)
        {
            for (int i = 0; i + n <= q.length(); i++)
            {
                String term = q.substring(i, i + n);
                List<Long> ids = inverted.get(term);
                if (ids == null)
                {
                    continue;
                }
                for (Long id : ids)
                {
                    score.put(id, score.getOrDefault(id, 0) + 1);
                }
            }
        }

        if (score.isEmpty())
        {
            return List.of();
        }

        List<Long> all = new ArrayList<>(score.keySet());
        all.sort((a, b) -> Integer.compare(score.getOrDefault(b, 0), score.getOrDefault(a, 0)));
        int to = Math.min(limit, all.size());
        return all.subList(0, to);
    }

    private String normalize(String s)
    {
        return s.trim().toLowerCase();
    }
}

