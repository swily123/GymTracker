package com.swily.gymtracker

object WeightUtils {
    private const val KG_TO_LB = 2.20462f

    fun convert(kg: Float, useKg: Boolean): Float {
        return if (useKg) kg else kg * KG_TO_LB
    }

    fun format(kg: Float, useKg: Boolean): String {
        val value = convert(kg, useKg)
        return "${value.toInt()} ${unit(useKg)}"
    }

    fun unit(useKg: Boolean): String {
        return if (useKg) "кг" else "lb"
    }

    fun toLb(kg: Float): Float = kg * KG_TO_LB
    fun toKg(lb: Float): Float = lb / KG_TO_LB
}