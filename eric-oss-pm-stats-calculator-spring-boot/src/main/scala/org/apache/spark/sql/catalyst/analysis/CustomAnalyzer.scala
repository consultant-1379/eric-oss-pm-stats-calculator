/*
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

package org.apache.spark.sql.catalyst.analysis


import org.apache.spark.sql.catalyst.catalog.{CatalogDatabase, InMemoryCatalog, SessionCatalog}
import org.apache.spark.sql.connector.catalog._
import org.apache.spark.sql.connector.catalog.functions.UnboundFunction
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util;


object CustomAnalyzer extends Analyzer(

  new CatalogManager(
    CustomFakeV2SessionCatalog, new SessionCatalog(new InMemoryCatalog, SimpleFunctionRegistryProvider.simpleFunctionRegistry(), TableFunctionRegistry.builtin) {
      override def createDatabase(dbDefinition: CatalogDatabase, ignoreIfExists: Boolean): Unit = {}
    })) {

  override def resolver: Resolver = caseSensitiveResolution
}

/**
 * Custom implementation based on [[FakeV2SessionCatalog]].
 */
object CustomFakeV2SessionCatalog extends TableCatalog with FunctionCatalog {

  override def listTables(namespace: Array[String]): Array[Identifier] = {
    throw new UnsupportedOperationException("CustomFakeV2SessionCatalog.listTables")
  }

  override def loadTable(ident: Identifier): Table = {
    throw NoSuchTableException(ident.toString)
  }

  override def createTable(ident: Identifier, schema: StructType, partitions: Array[Transform], properties: util.Map[String, String]): Table = {
    throw new UnsupportedOperationException("CustomFakeV2SessionCatalog.createTable")
  }

  override def alterTable(ident: Identifier, changes: TableChange*): Table = {
    throw new UnsupportedOperationException("CustomFakeV2SessionCatalog.alterTable")
  }

  override def dropTable(ident: Identifier): Boolean = {
    throw new UnsupportedOperationException("CustomFakeV2SessionCatalog.alterTable")
  }

  override def renameTable(oldIdent: Identifier, newIdent: Identifier): Unit = {
    throw new UnsupportedOperationException("CustomFakeV2SessionCatalog.renameTable")
  }

  override def initialize(name: String, options: CaseInsensitiveStringMap): Unit = {
    throw new UnsupportedOperationException("CustomFakeV2SessionCatalog.initialize")
  }

  override def name(): String = CatalogManager.SESSION_CATALOG_NAME

  override def listFunctions(namespace: Array[String]): Array[Identifier] = {
    throw new UnsupportedOperationException("CustomFakeV2SessionCatalog.listFunctions")
  }

  override def loadFunction(ident: Identifier): UnboundFunction = {
    throw new NoSuchFunctionException(ident)
  }
}