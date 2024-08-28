/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.loader.model;

import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataProviderType;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataService;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataSpace;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataType;
import com.ericsson.oss.air.pm.stats.test.loader.model.SchemaObjects.MessageDataTopic;

import lombok.Data;

@Data
public class MessageSchemaResponse {
    private Integer id;
    private DataSpace dataSpace;
    private DataService dataService;
    private DataProviderType dataProviderType;
    private MessageDataTopic messageDataTopic;
    private DataType dataType;
    private String specificationReference;
}