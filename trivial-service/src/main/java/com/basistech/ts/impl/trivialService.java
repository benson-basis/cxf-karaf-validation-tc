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


