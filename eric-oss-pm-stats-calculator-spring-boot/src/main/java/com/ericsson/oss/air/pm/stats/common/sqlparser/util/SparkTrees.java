/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.util;

import static lombok.AccessLevel.PRIVATE;

import java.util.stream.Stream;

import lombok.NoArgsConstructor;
import org.apache.spark.sql.catalyst.trees.TreeNode;

@NoArgsConstructor(access = PRIVATE)
public final class SparkTrees {

    public static <T extends TreeNode<T>> Stream<T> asJavaStreamWithChildren(final T treeNode) {
        return Stream.concat(Stream.of(treeNode),
                SparkCollections.asJavaStream(treeNode.children()).flatMap(SparkTrees::asJavaStreamWithChildren));
    }
}
