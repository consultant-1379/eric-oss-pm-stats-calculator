/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Interface only for OpenApi information.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "KPI Definition and Calculation API",
                version = "1.1.0",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "x-api-id", value = "14debd9c-19df-4868-bbb6-80c133efbd33"),
                                @ExtensionProperty(name = "x-audience", value = "internal")
                        })
                },
                contact = @Contact(name = "Team Velociraptors", email = "PDLVELOCIR@pdl.internal.ericsson.com"),
                license = @License(name = "COPYRIGHT Ericsson 2024", url = "https://www.ericsson.com/en/legal")),
        servers = @Server(url = "http://eric-pm-stats-calculator:8080/kpi-handling"))

public interface KpiResource {
}
