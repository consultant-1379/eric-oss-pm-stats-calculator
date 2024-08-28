/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.model.collection;

import java.util.UUID;

public class CollectionIdProxy {
    //TODO: This is a temporary class to avoid duplications when implementing colletionId as a pre-multiple-RApp-step.
    // It MUST BE DELETED later on, helping us getting rid of all the proxy collectionIds, eliminating the possibility
    // of using wrong collectionIds when the multiple RApp stage is live.

    public static final UUID COLLECTION_ID = UUID.fromString("29dc1bbf-7cdf-421b-8fc9-e363889ada79");
}
