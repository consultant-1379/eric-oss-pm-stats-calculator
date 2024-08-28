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

import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataCategory;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataProviderType;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataService;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataServiceInstance;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataSpace;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataType;
import com.ericsson.oss.air.pm.stats.test.loader.model.SchemaObjects.MessageDataTopic;
import com.ericsson.oss.air.pm.stats.test.loader.model.SchemaObjects.MessageSchema;
import com.ericsson.oss.air.pm.stats.test.loader.model.SchemaObjects.MessageStatusTopic;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateSchemaCommand {
    private DataSpace dataSpace;
    private DataProviderType dataProviderType;
    private MessageStatusTopic messageStatusTopic;
    private MessageDataTopic messageDataTopic;
    private DataType dataType;
    private DataCategory dataCategory;
    private MessageSchema messageSchema;
    private DataService dataService;
    private DataServiceInstance dataServiceInstance;
}