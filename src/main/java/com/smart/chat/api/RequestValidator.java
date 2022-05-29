package com.smart.chat.api;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@Component
public class RequestValidator {

    public <T> boolean isInvalid(T object) {
        try(ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.usingContext().getValidator();
            Set<ConstraintViolation<T>> constraintViolations = validator.validate(object);

            return !CollectionUtils.isEmpty(constraintViolations);
        }
    }

}
