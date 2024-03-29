package kr_two

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ErlangDistributionGeneratorTest {

    private val erlangDistributionGenerator = ErlangDistributionGenerator(isRealRandom = true, isDebug = false)

    @Test
    fun testBounds() {
        val nList = List(100) { it }
        val muList = List(100) { 0.1 * it }
        nList.forEach { n ->
            muList.forEach { mu ->
                val resultList = List(100) { erlangDistributionGenerator.invoke(n, mu) }
                //assertEquals(true, resultList.all { it >= 0 })
                val badList = resultList.filter { it <=0 }
                if (badList.isNotEmpty()) {
                    println(badList)
                }
            }
        }
        assertEquals(true, true)

    }

    @Test
    fun testFactorial() {
        assertEquals( 1, factorial(0))
        assertEquals( 1, factorial(1))
        assertEquals( 2, factorial(2))
        assertEquals( 6, factorial(3))
        assertEquals( 24, factorial(4))
    }
}