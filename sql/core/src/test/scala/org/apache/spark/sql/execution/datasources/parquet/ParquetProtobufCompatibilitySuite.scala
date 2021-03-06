/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.datasources.parquet

import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.test.SharedSQLContext
/**
 * Protobuf兼容测试
 */
class ParquetProtobufCompatibilitySuite extends ParquetCompatibilityTest with SharedSQLContext {
  //读Parquet protobuf文件
  private def readParquetProtobufFile(name: String): DataFrame = {
    //println("========="+name)
    val url = Thread.currentThread().getContextClassLoader.getResource(name)    
    //println("===="+url)
    sqlContext.read.parquet(url.toString)
  }
  //未注释的原始类型数组
  test("unannotated array of primitive type") {
    checkAnswer(readParquetProtobufFile("old-repeated-int.parquet"), Row(Seq(1, 2, 3)))
  }
  //未注释的结构数组
  test("unannotated array of struct") {
    //readParquetProtobufFile("old-repeated-message.parquet").show()
    checkAnswer(
      readParquetProtobufFile("old-repeated-message.parquet"),
      Row(
        Seq(
          Row("First inner", null, null),
          Row(null, "Second inner", null),
          Row(null, null, "Third inner"))))

    checkAnswer(
      readParquetProtobufFile("proto-repeated-struct.parquet"),
      Row(
        Seq(
          Row("0 - 1", "0 - 2", "0 - 3"),
          Row("1 - 1", "1 - 2", "1 - 3"))))

    checkAnswer(
      readParquetProtobufFile("proto-struct-with-array-many.parquet"),
      Seq(
        Row(
          Seq(
            Row("0 - 0 - 1", "0 - 0 - 2", "0 - 0 - 3"),
            Row("0 - 1 - 1", "0 - 1 - 2", "0 - 1 - 3"))),
        Row(
          Seq(
            Row("1 - 0 - 1", "1 - 0 - 2", "1 - 0 - 3"),
            Row("1 - 1 - 1", "1 - 1 - 2", "1 - 1 - 3"))),
        Row(
          Seq(
            Row("2 - 0 - 1", "2 - 0 - 2", "2 - 0 - 3"),
            Row("2 - 1 - 1", "2 - 1 - 2", "2 - 1 - 3")))))
  }
  //带有未注释数组的struct
  test("struct with unannotated array") {
    checkAnswer(
      readParquetProtobufFile("proto-struct-with-array.parquet"),
      Row(10, 9, Seq.empty, null, Row(9), Seq(Row(9), Row(10))))
  }
  //未注释的数组,带有未注释的数组
  test("unannotated array of struct with unannotated array") {
    checkAnswer(
      readParquetProtobufFile("nested-array-struct.parquet"),
      Seq(
        Row(2, Seq(Row(1, Seq(Row(3))))),
        Row(5, Seq(Row(4, Seq(Row(6))))),
        Row(8, Seq(Row(7, Seq(Row(9)))))))
  }
  //未注释的字符串数组
  test("unannotated array of string") {
    checkAnswer(
      readParquetProtobufFile("proto-repeated-string.parquet"),
      Seq(
        Row(Seq("hello", "world")),
        Row(Seq("good", "bye")),
        Row(Seq("one", "two", "three"))))
  }
}
