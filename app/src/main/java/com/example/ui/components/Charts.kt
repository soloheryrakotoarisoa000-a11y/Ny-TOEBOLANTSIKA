package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CategorySpend
import com.example.ui.DaySpend
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
    categorySpends: List<CategorySpend>,
    totalAmount: Long,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (categorySpends.isEmpty() || totalAmount <= 0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aucune dépense enregistrée ce mois-ci",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(categorySpends) {
        animatedProgress.animateTo(1f, animationSpec = tween(700))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(190.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                var startAngle = -90f
                val strokeWidth = 26.dp.toPx()

                categorySpends.forEach { spend ->
                    val sweepAngle = (spend.percentage / 100f) * 360f * animatedProgress.value
                    if (sweepAngle > 0f) {
                        val color = CategoryIconHelper.parseColor(spend.category.colorHex)
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += sweepAngle
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Dépenses",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(totalAmount, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend chips
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categorySpends.take(6).forEach { item ->
                val color = CategoryIconHelper.parseColor(item.category.colorHex)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.category.name} (${String.format("%.1f", item.percentage)}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SevenDaysBarChart(
    daysData: List<DaySpend>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val maxSpent = daysData.maxOfOrNull { it.amount } ?: 0L
    val hasData = maxSpent > 0L

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            daysData.forEach { day ->
                val ratio = if (hasData) (day.amount.toFloat() / maxSpent.toFloat()).coerceIn(0.04f, 1f) else 0.04f
                val isToday = day.label == "Auj."
                val barColor = if (day.amount > 0) {
                    if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (day.amount > 0) {
                        Text(
                            text = if (day.amount >= 1000) "${day.amount / 1000}k" else "${day.amount}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(84.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Background track
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        )
                        // Bar fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(ratio)
                                .clip(RoundedCornerShape(6.dp))
                                .background(barColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = day.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
