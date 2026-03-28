package com.travel.service.impl;

import com.travel.storage.InMemoryStore;
import com.travel.model.dto.auth.InterestItemRequest;
import com.travel.model.dto.auth.LoginRequest;
import com.travel.model.dto.auth.RegisterRequest;
import com.travel.model.dto.auth.UpdateInterestRequest;
import com.travel.model.entity.User;
import com.travel.model.entity.UserInterest;
import com.travel.model.vo.auth.InterestItemVO;
import com.travel.model.vo.UserVO;
import com.travel.security.JwtUtil;
import com.travel.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现。
 */
@Service
public class UserServiceImpl implements UserService
{

    private final InMemoryStore store;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public UserServiceImpl(InMemoryStore store,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil)
    {
        this.store = store;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterRequest request)
    {
        if (store.findUserByUsername(request.getUsername()) != null ||
            store.findUserByEmail(request.getEmail()) != null)
        {
            throw new IllegalArgumentException("用户名或邮箱已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setRole("USER");
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        store.insertUser(user);
        return toUserVO(user);
    }

    @Override
    public String login(LoginRequest request)
    {
        User user = store.findUserByUsername(request.getUsername());
        if (user == null)
        {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
        {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInterests(Long userId, UpdateInterestRequest request)
    {
        List<InterestItemRequest> items = request.getInterests();
        LocalDateTime now = LocalDateTime.now();
        List<UserInterest> interests = new ArrayList<>();
        for (InterestItemRequest item : items)
        {
            String type = item.getType();
            if (StringUtils.isBlank(type))
            {
                continue;
            }
            UserInterest interest = new UserInterest();
            interest.setUserId(userId);
            interest.setInterestType(type.trim());
            interest.setWeight(item.getWeight() == null ? 1.0 : item.getWeight());
            interest.setCreateTime(now);
            interests.add(interest);
        }
        store.replaceUserInterests(userId, interests);
    }

    @Override
    public List<InterestItemVO> getInterests(Long userId)
    {
        Map<String, Double> interests = store.getUserInterests(userId);
        if (interests == null || interests.isEmpty())
        {
            return List.of();
        }

        List<InterestItemVO> out = new ArrayList<>(interests.size());
        for (Map.Entry<String, Double> entry : interests.entrySet())
        {
            InterestItemVO item = new InterestItemVO();
            item.setType(entry.getKey());
            item.setWeight(entry.getValue());
            out.add(item);
        }
        return out;
    }

    @Override
    public UserVO findByUsername(String username)
    {
        User user = store.findUserByUsername(username);
        if (user == null)
        {
            return null;
        }
        return toUserVO(user);
    }

    private UserVO toUserVO(User user)
    {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        return vo;
    }
}

