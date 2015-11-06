/******************************************************************************
 * * This data and information is proprietary to, and a valuable trade secret
 * * of, Basis Technology Corp.  It is given in confidence by Basis Technology
 * * and may only be used as permitted under the license agreement under which
 * * it has been distributed, and in no other way.
 * *
 * * Copyright (c) 2015 Basis Technology Corporation All rights reserved.
 * *
 * * The technical data and information provided herein are provided with
 * * `limited rights', and the computer software provided herein is provided
 * * with `restricted rights' as those terms are defined in DAR and ASPR
 * * 7-104.9(a).
 ******************************************************************************/

package com.basistech.ws.beanvalidation.impl;

import com.basistech.ws.beanvalidation.BeanValidation;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * We isolate interactions with the nasty SPI situation in here.
 */
@Component
public class BeanValidationImpl implements BeanValidation {

    private ValidatorFactory factory;

    @Activate
    public void activator() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(BeanValidationImpl.class.getClassLoader());
            factory = Validation.buildDefaultValidatorFactory();
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public Validator createValidator() {
        return factory.getValidator();
    }
}
