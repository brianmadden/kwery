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

import javax.sql.DataSource
import com.github.andrewoma.kwery.core.dialect.Dialect
import org.junit.Test as test
import java.sql.Time
import java.sql.Date
import java.sql.Timestamp
import com.github.andrewoma.kommon.collection.plus
import kotlin.test.assertEquals

abstract class AbstractDialectTest(dataSource: DataSource, dialect: Dialect) : AbstractSessionTest(dataSource, dialect) {
    abstract val sql: String

    override fun afterSessionSetup() = initialise(dialect.javaClass.getName()) {

        for (statement in sql.split(";")) {
            session.update(statement)
        }
    }

    data class Value(val time: Time, val date: Date, val timestamp: Timestamp, val binary: String,
                     val varchar: String, val blob: String, val clob: String, val ints: List<Int>) {
    }

    test fun `Bindings to literals should return the same values when fetched`() {
        val now = System.currentTimeMillis()
        val value = Value(Time(now), Date(now), Timestamp(now), "binary",
                "var'char", "blob", "clob", listOf(1, 2, 3))

        //language=SQL
        val sql = """
            INSERT INTO dialect_test (id, time_col, date_col, timestamp_col, binary_col, varchar_col, blob_col, clob_col, array_col)
            VALUES (:id, :time_col, :date_col, :timestamp_col, :binary_col, :varchar_col, :blob_col, :clob_col, :array_col)
        """

        val params = mapOf(
                "time_col" to value.time,
                "date_col" to value.date,
                "timestamp_col" to value.timestamp,
                "binary_col" to value.binary.toByteArray(Charsets.UTF_8),
                "varchar_col" to value.varchar,
                "blob_col" to toBlob(value.blob),
                "clob_col" to toClob(value.clob),
                "array_col" to session.connection.createArrayOf("int", value.ints.copyToArray())
        )

        session.update(sql, params + mapOf("id" to "params"))

        val literalSql = session.bindParameters(sql, params + mapOf("id" to "literal"))
        session.update(literalSql, mapOf())

        val byParams = findById("params")
        val byLiteral = findById("literal")

        assertEqualValues(byParams, value)
        assertEqualValues(byLiteral, value)
    }

    private fun findById(id: String): Value {
        return session.select("select * from dialect_test where id = '$id'") { row ->
            Value(row.time("time_col"),
                    row.date("date_col"),
                    row.timestamp("timestamp_col"),
                    String(row.bytes("binary_col"), Charsets.UTF_8),
                    row.string("varchar_col"),
                    fromBlob(row, "blob_col"),
                    fromClob(row, "clob_col"),
                    row.array<Int>("array_col"))
        }.single()
    }

    private fun toBlob(data: String) = try {
        session.connection.createBlob().let { it.setBytes(1, data.toByteArray(Charsets.UTF_8)); it }
    } catch(e: Exception) {
        data.toByteArray(Charsets.UTF_8)
    }

    private fun fromBlob(row: Row, column: String) = try {
        row.blob(column).let { String(it.getBytes(1, it.length().toInt()), Charsets.UTF_8) }
    } catch(e: Exception) {
        String(row.bytes(column), Charsets.UTF_8)
    }

    private fun fromClob(row: Row, column: String) = try {
        row.clob(column).let { it.getSubString(1, it.length().toInt()).trim() }
    } catch(e: Exception) {
        String(row.bytes(column), Charsets.UTF_8)
    }

    private fun toClob(data: String) = try {
        session.connection.createClob().let { it.setString(1, data); it }
    } catch(e: Exception) {
        data
    }

    private fun assertEqualValues(actual: Value, expected: Value) {
        println(expected)
        println(actual)

        // Dates and times aren't equal by equality with hsqldb, so test the strings
        assertEquals(expected.time.toString(), actual.time.toString())
        assertEquals(expected.date.toString(), actual.date.toString())
        assertEquals(expected.timestamp.toString(), actual.timestamp.toString())
        assertEquals(expected.binary, actual.binary)
        assertEquals(expected.varchar, actual.varchar)
        assertEquals(expected.blob, actual.blob)
        assertEquals(expected.clob, actual.clob)
        assertEquals(expected.ints, actual.ints)
    }
}