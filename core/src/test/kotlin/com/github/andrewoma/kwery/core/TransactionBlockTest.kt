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

import org.junit.Test
import kotlin.test.assertEquals

class TransactionBlockTest : AbstractFilmSessionTest() {
    override var startTransactionByDefault: Boolean = false

    @Test fun `setRollbackOnly should roll back transaction`() {
        val actor = session.transaction { t ->
            val inserted = insert(Actor("Kate", "Beckinsale"))
            t.rollbackOnly = true
            assertEquals(1, selectActors(setOf(inserted.id)).size)
            inserted
        }
        assertEquals(0, selectActors(setOf(actor.id)).size)
    }

    @Test fun `exception should roll back transaction`() {
        var id: Int? = null
        try {
            session.transaction { t ->
                val actor = insert(Actor("Kate", "Beckinsale"))
                id = actor.id
                assertEquals(1, selectActors(setOf(actor.id)).size)
                throw RuntimeException("From block")
            }
        } catch(e: Exception) {
            assertEquals("From block", e.message)
        }
        assertEquals(0, selectActors(setOf(id)).size)
    }

    @Test fun `block should be implicitly committed`() {
        val actor = session.transaction { t ->
            val inserted = insert(Actor("Kate", "Beckinsale"))
            assertEquals(1, selectActors(setOf(inserted.id)).size)
            inserted
        }
        assertEquals(1, selectActors(setOf(actor.id)).size)
    }
}

