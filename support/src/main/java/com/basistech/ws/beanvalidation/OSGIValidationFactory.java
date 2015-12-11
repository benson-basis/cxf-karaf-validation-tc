/*
* Copyright 2015 Basis Technology Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.basistech.ws.beanvalidation;

import org.apache.cxf.validation.BeanValidationProvider;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.bootstrap.ProviderSpecificBootstrap;

/**
 * utility class for using hibernate validation in OSGi.
 */
public final class OSGIValidationFactory {
    static ValidatorFactory validatorFactory;

    private OSGIValidationFactory() {
        //
    }

    /**
     * returns the validatorfactory.
     * @return the singleton validatorfactory
     */
    public static synchronized ValidatorFactory getValidatorFactory() {
        if (validatorFactory == null) {
            final ProviderSpecificBootstrap<HibernateValidatorConfiguration> validationBootStrap = Validation
                    .byProvider(HibernateValidator.class);

            // bootstrap to properly resolve in an OSGi environment
            validationBootStrap
                    .providerResolver(HibernateValidationOSGIServicesProviderResolver
                            .getInstance());

            final HibernateValidatorConfiguration configure = validationBootStrap
                    .configure();
            validatorFactory = configure./*constraintValidatorFactory (new
                                    CDIAwareConstraintValidatorFactory ())
                                   .*/buildValidatorFactory();
        }
        return validatorFactory;
    }

    public static BeanValidationProvider newProvider() {
        return new BeanValidationProvider();
    }
}
