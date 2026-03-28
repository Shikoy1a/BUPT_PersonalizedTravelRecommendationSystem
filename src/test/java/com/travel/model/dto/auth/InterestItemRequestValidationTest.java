package com.travel.model.dto.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterestItemRequestValidationTest
{

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectNonPositiveWeight()
    {
        InterestItemRequest req = new InterestItemRequest();
        req.setType("美食");
        req.setWeight(0.0);

        Set<ConstraintViolation<InterestItemRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldRejectTooLargeWeight()
    {
        InterestItemRequest req = new InterestItemRequest();
        req.setType("美食");
        req.setWeight(5.1);

        Set<ConstraintViolation<InterestItemRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassValidWeight()
    {
        InterestItemRequest req = new InterestItemRequest();
        req.setType("美食");
        req.setWeight(1.8);

        Set<ConstraintViolation<InterestItemRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }
}
