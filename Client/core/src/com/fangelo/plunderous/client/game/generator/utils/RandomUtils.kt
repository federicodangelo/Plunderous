package com.fangelo.plunderous.client.game.generator.utils

import com.badlogic.gdx.math.MathUtils
import java.util.*

//Taken from libgdx MathUtils!!

class RandomUtils(val random: Random) {

    /** Returns a random number between 0 (inclusive) and the specified value (inclusive).  */
    fun random(range: Int): Int {
        return random.nextInt(range + 1)
    }

    /** Returns a random number between start (inclusive) and end (inclusive).  */
    fun random(start: Int, end: Int): Int {
        return start + random.nextInt(end - start + 1)
    }

    /** Returns a random number between 0 (inclusive) and the specified value (inclusive).  */
    fun random(range: Long): Long {
        return (random.nextDouble() * range).toLong()
    }

    /** Returns a random number between start (inclusive) and end (inclusive).  */
    fun random(start: Long, end: Long): Long {
        return start + (random.nextDouble() * (end - start)).toLong()
    }

    /** Returns a random boolean value.  */
    fun randomBoolean(): Boolean {
        return random.nextBoolean()
    }

    /** Returns true if a random value between 0 and 1 is less than the specified value.  */
    fun randomBoolean(chance: Float): Boolean {
        return MathUtils.random() < chance
    }

    /** Returns random number between 0.0 (inclusive) and 1.0 (exclusive).  */
    fun random(): Float {
        return random.nextFloat()
    }

    /** Returns a random number between 0 (inclusive) and the specified value (exclusive).  */
    fun random(range: Float): Float {
        return random.nextFloat() * range
    }

    /** Returns a random number between start (inclusive) and end (exclusive).  */
    fun random(start: Float, end: Float): Float {
        return start + random.nextFloat() * (end - start)
    }

    /** Returns -1 or 1, randomly.  */
    fun randomSign(): Int {
        return 1 or (random.nextInt() shr 31)
    }

    /** Returns a triangularly distributed random number between -1.0 (exclusive) and 1.0 (exclusive), where values around zero are
     * more likely.
     *
     *
     * This is an optimized version of [randomTriangular(-1, 1, 0)][.randomTriangular]  */
    fun randomTriangular(): Float {
        return random.nextFloat() - random.nextFloat()
    }

    /** Returns a triangularly distributed random number between `-max` (exclusive) and `max` (exclusive), where values
     * around zero are more likely.
     *
     *
     * This is an optimized version of [randomTriangular(-max, max, 0)][.randomTriangular]
     * @param max the upper limit
     */
    fun randomTriangular(max: Float): Float {
        return (random.nextFloat() - random.nextFloat()) * max
    }

    /** Returns a triangularly distributed random number between `min` (inclusive) and `max` (exclusive), where the
     * `mode` argument defaults to the midpoint between the bounds, giving a symmetric distribution.
     *
     *
     * This method is equivalent of [randomTriangular(min, max, (min + max) * .5f)][.randomTriangular]
     * @param min the lower limit
     * @param max the upper limit
     */
    fun randomTriangular(min: Float, max: Float): Float {
        return randomTriangular(min, max, (min + max) * 0.5f)
    }

    /** Returns a triangularly distributed random number between `min` (inclusive) and `max` (exclusive), where values
     * around `mode` are more likely.
     * @param min the lower limit
     * @param max the upper limit
     * @param mode the point around which the values are more likely
     */
    fun randomTriangular(min: Float, max: Float, mode: Float): Float {
        val u = random.nextFloat()
        val d = max - min
        return if (u <= (mode - min) / d) min + Math.sqrt((u * d * (mode - min)).toDouble()).toFloat() else max - Math.sqrt(((1 - u) * d * (max - mode)).toDouble()).toFloat()
    }

}