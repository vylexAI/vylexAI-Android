package com.vylexai.app.ui.screens

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/** "24.38" for display (2 dp). */
internal fun formatBsaiAmount(amount: BigDecimal): String =
    amount.setScale(2, RoundingMode.HALF_UP).toPlainString()

/** "1,284" — grouped thousands in US locale (works for all Western separators we care about). */
internal fun formatInt(value: Int): String =
    NumberFormat.getIntegerInstance(Locale.US).format(value)
