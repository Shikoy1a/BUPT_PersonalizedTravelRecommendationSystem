package com.travel.service.impl;

import com.travel.common.PageData;
import com.travel.storage.InMemoryStore;
import com.travel.model.entity.Building;
import com.travel.model.entity.Food;
import com.travel.model.entity.Road;
import com.travel.model.entity.ScenicArea;
import com.travel.service.AdminService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端服务实现。
 */
@Service
public class AdminServiceImpl implements AdminService
{

    private final InMemoryStore store;

    public AdminServiceImpl(InMemoryStore store)
    {
        this.store = store;
    }

    @Override
    public ScenicArea addScenicArea(ScenicArea scenicArea)
    {
        LocalDateTime now = LocalDateTime.now();
        scenicArea.setCreateTime(now);
        scenicArea.setUpdateTime(now);
        store.insertScenicArea(scenicArea);
        return scenicArea;
    }

    @Override
    public PageData<ScenicArea> listScenicAreas(Integer page, Integer size, String type)
    {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size <= 0 ? 10 : Math.min(size, 50);
        int offset = (p - 1) * s;
        List<ScenicArea> list = type != null && !type.isBlank()
            ? store.findScenicAreasByType(type)
            : store.findAllScenicAreas();

        list.sort((a, b) ->
        {
            LocalDateTime ta = a.getCreateTime();
            LocalDateTime tb = b.getCreateTime();
            if (ta == null && tb == null)
            {
                return 0;
            }
            if (ta == null)
            {
                return 1;
            }
            if (tb == null)
            {
                return -1;
            }
            return tb.compareTo(ta);
        });

        int total = list.size();
        if (offset >= total)
        {
            return new PageData<>(List.of(), (long) total);
        }
        int to = Math.min(offset + s, total);
        return new PageData<>(list.subList(offset, to), (long) total);
    }

    @Override
    public Building addBuilding(Building building)
    {
        LocalDateTime now = LocalDateTime.now();
        building.setCreateTime(now);
        building.setUpdateTime(now);
        store.insertBuilding(building);
        return building;
    }

    @Override
    public Road addRoad(Road road)
    {
        LocalDateTime now = LocalDateTime.now();
        road.setCreateTime(now);
        road.setUpdateTime(now);
        store.insertRoad(road);
        return road;
    }

    @Override
    public Food addFood(Food food)
    {
        LocalDateTime now = LocalDateTime.now();
        food.setCreateTime(now);
        food.setUpdateTime(now);
        store.insertFood(food);
        return food;
    }
}

