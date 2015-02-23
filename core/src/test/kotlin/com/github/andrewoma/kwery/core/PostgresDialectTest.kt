/*
 * Copyright (c) 2015 Andrew O'Malley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.andrewoma.kwery.core

import com.github.andrewoma.kwery.core.dialect.PostgresDialect

class PostgresDialectTest : AbstractDialectTest(postgresDataSource, PostgresDialect()) {
    //language=SQL
    override val sql = """
            DROP TABLE IF EXISTS dialect_test;

            CREATE TABLE dialect_test (
              id            VARCHAR(255),
              time_col      TIME,
              date_col      DATE,
              timestamp_col TIMESTAMP,
              binary_col    bytea,
              varchar_col   VARCHAR(1000),
              blob_col      bytea,
              clob_col      TEXT,
              array_col     INT ARRAY
            );

            DROP TABLE IF EXISTS test;

            CREATE TABLE test (
              id            VARCHAR(255),
              value         VARCHAR(255)
            )
        """
}