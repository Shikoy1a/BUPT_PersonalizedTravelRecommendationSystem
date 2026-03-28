package com.travel.service.impl;

import com.travel.model.dto.auth.InterestItemRequest;
import com.travel.model.dto.auth.UpdateInterestRequest;
import com.travel.model.entity.UserInterest;
import com.travel.security.JwtUtil;
import com.travel.storage.InMemoryStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest
{

    @Mock
    private InMemoryStore store;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Test
    void updateInterestsShouldUseDefaultWeightWhenWeightIsNull()
    {
        UserServiceImpl service = new UserServiceImpl(store, passwordEncoder, jwtUtil);

        InterestItemRequest item = new InterestItemRequest();
        item.setType("  美食  ");
        item.setWeight(null);

        UpdateInterestRequest request = new UpdateInterestRequest();
        request.setInterests(List.of(item));

        service.updateInterests(101L, request);

        ArgumentCaptor<List<UserInterest>> captor = ArgumentCaptor.forClass(List.class);
        verify(store).replaceUserInterests(org.mockito.ArgumentMatchers.eq(101L), captor.capture());

        List<UserInterest> saved = captor.getValue();
        assertEquals(1, saved.size());
        assertEquals("美食", saved.get(0).getInterestType());
        assertEquals(1.0, saved.get(0).getWeight());
        assertNotNull(saved.get(0).getCreateTime());
    }

    @Test
    void updateInterestsShouldPersistProvidedWeight()
    {
        UserServiceImpl service = new UserServiceImpl(store, passwordEncoder, jwtUtil);

        InterestItemRequest item = new InterestItemRequest();
        item.setType("历史");
        item.setWeight(2.3);

        UpdateInterestRequest request = new UpdateInterestRequest();
        request.setInterests(List.of(item));

        service.updateInterests(202L, request);

        ArgumentCaptor<List<UserInterest>> captor = ArgumentCaptor.forClass(List.class);
        verify(store).replaceUserInterests(org.mockito.ArgumentMatchers.eq(202L), captor.capture());

        List<UserInterest> saved = captor.getValue();
        assertEquals(1, saved.size());
        assertEquals("历史", saved.get(0).getInterestType());
        assertEquals(2.3, saved.get(0).getWeight());
    }
}
