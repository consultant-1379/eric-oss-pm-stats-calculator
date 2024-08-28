/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.configuration.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.ericsson.oss.air.pm.stats.common.rest.RestExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class RestExecutorProducer {

    @Produces
    public RestExecutor restExecutor() {
        return new RestExecutor();
    }

}
