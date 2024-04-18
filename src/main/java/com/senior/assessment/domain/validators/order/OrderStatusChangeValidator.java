package com.senior.assessment.domain.validators.order;

import com.senior.assessment.domain.enums.OrderStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class OrderStatusChangeValidator implements ConstraintValidator<OrderStatusChange, OrderStatus> {
    private OrderStatus[] allowedStatuses;

    @Override
    public void initialize(OrderStatusChange constraint) {
        this.allowedStatuses = constraint.anyOf();
    }

    @Override
    public boolean isValid(OrderStatus status, ConstraintValidatorContext context) {
        return status != null && Arrays.asList(allowedStatuses).contains(status);
    }
}