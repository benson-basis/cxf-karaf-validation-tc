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

package com.basistech.ws.beanvalidation;

import org.apache.bval.jsr.ApacheValidationProvider;

import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ValidationProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * utility class for using hibernate validation in OSGi.
 */
public final class OSGIValidationFactory {
    private OSGIValidationFactory() {
        //
    }

    static class OSGIServiceDiscoverer implements ValidationProviderResolver {

        @Override
        public List<ValidationProvider<?>> getValidationProviders() {
            List<ValidationProvider<?>> providers = new ArrayList<>();
            providers.add(new ApacheValidationProvider());
            return providers;
        }
    }

    public static ValidatorFactory newValidatorFactory() {
        javax.validation.Configuration<?> config = Validation.byDefaultProvider()
                .providerResolver(new OSGIServiceDiscoverer())
                .configure();

        return config.buildValidatorFactory();
    }
}
