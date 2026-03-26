package com.travel.storage.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前缀 Trie 索引：把 word 的前缀查询映射到候选 id 列表。
 *
 * <p>
 * 适用于“前缀/模糊（前缀）”类查询。
 * </p>
 */
public class PrefixTrieIdIndex
{

    private final Node root = new Node();

    public void insert(String word, long id)
    {
        if (word == null)
        {
            return;
        }
        String w = normalize(word);
        if (w.isEmpty())
        {
            return;
        }

        Node cur = root;
        for (int i = 0; i < w.length(); i++)
        {
            char c = w.charAt(i);
            cur.children.computeIfAbsent(c, k -> new Node());
            cur = cur.children.get(c);
            cur.candidateIds.add(id);
        }
    }

    /**
     * 查询 prefix 对应的候选 ids。
     */
    public List<Long> search(String prefix)
    {
        if (prefix == null)
        {
            return List.of();
        }
        String p = normalize(prefix);
        if (p.isEmpty())
        {
            return List.of();
        }

        Node cur = root;
        for (int i = 0; i < p.length(); i++)
        {
            char c = p.charAt(i);
            Node next = cur.children.get(c);
            if (next == null)
            {
                return List.of();
            }
            cur = next;
        }
        return new ArrayList<>(cur.candidateIds);
    }

    private String normalize(String s)
    {
        return s.trim().toLowerCase();
    }

    private static final class Node
    {
        private final Map<Character, Node> children = new HashMap<>();
        private final List<Long> candidateIds = new ArrayList<>();
    }
}

