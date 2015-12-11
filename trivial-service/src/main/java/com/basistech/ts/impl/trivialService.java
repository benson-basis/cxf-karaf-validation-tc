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

package com.basistech.ts.impl;

import com.basistech.ws.beanvalidation.OSGIValidationFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.impl.WebApplicationExceptionMapper;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;

import javax.validation.ConstraintViolation;
import javax.ws.rs.GET;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceException;
import java.util.Collections;
import java.util.Set;

import static org.slf4j.LoggerFactory.*;

/**
 *
 */
@Component(service = {}, immediate = true)
public class TrivialService {
    private static final Logger LOG = getLogger(TrivialService.class);
    private Server server;

    @GET
    public Response validate() {

        ToValidate tv = new ToValidate(0);

        Set<ConstraintViolation<ToValidate>> constraintViolations = OSGIValidationFactory.newValidator().validate(tv);
        if (constraintViolations.size() > 0) {
            StringBuilder violationMessages = new StringBuilder();
            for (ConstraintViolation<ToValidate> constraintViolation : constraintViolations) {
                violationMessages.append(constraintViolation.getPropertyPath())
                        .append(": ").append(constraintViolation.getMessage()).append("\n");
            }
            throw new WebServiceException(violationMessages.toString());
        }
        return Response.ok().build();
    }

    @Activate
    public void activate() {
        LOG.info("activate");
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setProvider(new WebApplicationExceptionMapper());
        sf.setServiceBeans(Collections.singletonList((Object)this));

        String url = "/validate";
        LOG.info(String.format("%s at %s", getClass().getName(), url));
        sf.setAddress(url);
        server = sf.create();
    }

    @Deactivate
    public void deactivate() {
        LOG.info("deactivate");
        server.stop();
    }
}


