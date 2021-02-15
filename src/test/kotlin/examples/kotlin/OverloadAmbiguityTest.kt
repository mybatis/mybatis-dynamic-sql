/*
 *    Copyright 2016-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValueHolder<T>(private val supplier: () -> T) {
    fun value() = supplier.invoke()
}

class HolderBuilder<T> {
    lateinit var theThing: ValueHolder<T>

    fun stepTwo(innerValue: T) {
        theThing = ValueHolder { innerValue }
    }
}

fun <T> buildIt(value: T): ValueHolder<T> = ValueHolder { value }

fun <T> buildIt(valueSupplier: () -> T): ValueHolder<T> = ValueHolder(valueSupplier)

fun <T> buildIt(valueHolderBuilder: HolderBuilder<T>.() -> Unit): ValueHolder<T> {
    val b = HolderBuilder<T>()
    valueHolderBuilder(b)
    return b.theThing
}

class Tests {
    @Test
    fun testOne() {
        val v = buildIt(3)

        assertThat(v.value()).isEqualTo(3)
    }

    @Test
    fun testTwo() {
        data class Fred (val id: Int)

        val f = Fred(3)

        val v = buildIt(f::id)

        assertThat(v.value()).isEqualTo(3)
    }

//    @Test
//    fun testTwoProblem() {
//        val v: ValueHolder<Int> = buildIt { 3 }
//
//        assertThat(v.value()).isEqualTo(3)
//    }

    @Test
    fun testTwoProblemFixOne() {
        val v = buildIt(valueSupplier = { 3 } )

        assertThat(v.value()).isEqualTo(3)
    }

    @Test
    fun testTwoFixTwo() {
        val v = buildIt(fun () = 3 )

        assertThat(v.value()).isEqualTo(3)
    }

//    @Test
//    fun testThreeProblem() {
//        val v = buildIt<Int> {
//            stepTwo(3)
//        }
//
//        assertThat(v.value()).isEqualTo(3)
//    }

    @Test
    fun testThreeFixed() {
        val v = buildIt(fun(b: HolderBuilder<Int>) = b.stepTwo(3))

        assertThat(v.value()).isEqualTo(3)
    }
}
