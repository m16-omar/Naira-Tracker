package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CurrencyUtils
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.DailySpending
import com.example.ui.viewmodel.UiState
import kotlin.math.max

@Composable
fun AnalyticsScreen(
    uiState: UiState,
    onUpdateBudget: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBudgetDialog by remember { mutableStateOf(false) }
    var selectedDaySpending by remember { mutableStateOf<DailySpending?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // 1. Budget Alert & Progress Section
        item {
            BudgetOverviewCard(
                uiState = uiState,
                onEditBudgetClick = { showBudgetDialog = true }
            )
        }

        // 2. Prominent Card showing "Top Spending Category" this month
        item {
            TopSpendingCategoryCard(uiState = uiState)
        }

        // 3. Bar Chart showing daily spending for the last 30 days
        item {
            DailySpendingBarChartCard(
                dailySpendings = uiState.dailySpendingLast30Days,
                selectedDay = selectedDaySpending,
                onSelectDay = { selectedDaySpending = it }
            )
        }

        // 4. Pie Chart showing spending breakdown by category
        item {
            CategoryPieChartCard(uiState = uiState)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showBudgetDialog) {
        SetBudgetDialog(
            currentBudget = uiState.monthlyBudget,
            onDismiss = { showBudgetDialog = false },
            onConfirm = {
                onUpdateBudget(it)
                showBudgetDialog = false
            }
        )
    }
}

@Composable
fun BudgetOverviewCard(
    uiState: UiState,
    onEditBudgetClick: () -> Unit
) {
    val progress = (uiState.spentThisMonth / max(1.0, uiState.monthlyBudget)).toFloat().coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "budgetProgress")
    val isOverBudget = uiState.isOverBudget

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_overview_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverBudget) Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surface
        ),
        border = if (isOverBudget) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AlertRed, ExpenseRed))) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOverBudget) Icons.Default.Warning else Icons.Default.ArrowOutward,
                        contentDescription = null,
                        tint = if (isOverBudget) AlertRed else EmeraldGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOverBudget) "BUDGET ALERT" else "Monthly Budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverBudget) AlertRed else MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onEditBudgetClick,
                    modifier = Modifier.size(32.dp).testTag("edit_budget_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Budget",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // High Visibility Alert Banner if over budget
            if (isOverBudget) {
                Spacer(modifier = Modifier.height(10.dp))
                val overAmount = uiState.spentThisMonth - uiState.monthlyBudget
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AlertRed.copy(alpha = 0.12f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚠️ You have exceeded your monthly budget by ${CurrencyUtils.formatNaira(overAmount)}! Consider reviewing your discretionary expenses.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AlertRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (isOverBudget) AlertRed else if (progress > 0.8f) WarningAmber else EmeraldGreenPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent this month",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatNaira(uiState.spentThisMonth),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverBudget) AlertRed else MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Monthly limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatNaira(uiState.monthlyBudget),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun TopSpendingCategoryCard(uiState: UiState) {
    val top = uiState.topSpendingCategory

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("top_spending_category_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF192A20),
                            Color(0xFF111713)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Top Spending Category",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                    Text(
                        text = "This Month",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGreenLight
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (top != null && top.amount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(top.categoryItem.color.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = top.categoryItem.icon,
                                contentDescription = top.categoryName,
                                tint = top.categoryItem.color,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = top.categoryName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Accounted for ${(top.percentage * 100).toInt()}% of your monthly expenses",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD1D5DB)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = CurrencyUtils.formatNaira(top.amount),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                } else {
                    Text(
                        text = "No expenses recorded this month yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}

@Composable
fun DailySpendingBarChartCard(
    dailySpendings: List<DailySpending>,
    selectedDay: DailySpending?,
    onSelectDay: (DailySpending?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_spending_barchart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Spending (Last 30 Days)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap any bar to inspect daily total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedDay != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreenPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${selectedDay.fullDate}: ${CurrencyUtils.formatNaira(selectedDay.amount)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val maxAmount = max(1000.0, dailySpendings.maxOfOrNull { it.amount } ?: 1000.0)

            // Custom Canvas Bar Chart for 30 Days
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dailySpendings) {
                            detectTapGestures { offset ->
                                val count = dailySpendings.size
                                if (count > 0) {
                                    val barWidthWithSpace = size.width / count
                                    val index = (offset.x / barWidthWithSpace).toInt().coerceIn(0, count - 1)
                                    onSelectDay(dailySpendings[index])
                                }
                            }
                        }
                ) {
                    val count = dailySpendings.size
                    if (count == 0) return@Canvas

                    val availableWidth = size.width
                    val availableHeight = size.height - 24.dp.toPx()
                    val barWidthWithGap = availableWidth / count
                    val barWidth = barWidthWithGap * 0.65f

                    // Draw subtle grid lines
                    val lineCount = 3
                    for (i in 1..lineCount) {
                        val y = availableHeight * (i.toFloat() / lineCount)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.35f),
                            start = Offset(0f, y),
                            end = Offset(availableWidth, y),
                            strokeWidth = 1f
                        )
                    }

                    // Baseline
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.4f),
                        start = Offset(0f, availableHeight),
                        end = Offset(availableWidth, availableHeight),
                        strokeWidth = 1.5f
                    )

                    dailySpendings.forEachIndexed { index, item ->
                        val barHeight = if (maxAmount > 0) {
                            ((item.amount / maxAmount) * availableHeight).toFloat().coerceAtLeast(if (item.amount > 0) 6f else 2f)
                        } else 2f

                        val left = (index * barWidthWithGap) + (barWidthWithGap - barWidth) / 2
                        val top = availableHeight - barHeight

                        val isSelected = selectedDay?.dateMillis == item.dateMillis

                        val barColor = when {
                            isSelected -> ExpenseRed
                            item.amount > 0 -> EmeraldGreenPrimary
                            else -> Color.LightGray.copy(alpha = 0.3f)
                        }

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Labels (30 days ago, 15 days ago, Today)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "30 days ago",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "15 days ago",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreenPrimary
                )
            }
        }
    }
}

@Composable
fun CategoryPieChartCard(uiState: UiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_piechart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "This Month",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val breakdown = uiState.categoryBreakdown
            if (breakdown.isEmpty() || uiState.spentThisMonth <= 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expense data for this month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Donut / Pie Chart in Center
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.size(180.dp)
                    ) {
                        var startAngle = -90f
                        val strokeWidth = 32.dp.toPx()
                        val arcSize = size.width - strokeWidth

                        breakdown.forEach { item ->
                            val sweepAngle = item.percentage * 360f
                            if (sweepAngle > 0) {
                                drawArc(
                                    color = item.categoryItem.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                startAngle += sweepAngle
                            }
                        }
                    }

                    // Center label inside donut
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Spent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyUtils.formatNairaCompact(uiState.spentThisMonth),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown list items
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    breakdown.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(item.categoryItem.color)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = item.categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = CurrencyUtils.formatNaira(item.amount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(item.categoryItem.color.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${(item.percentage * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = item.categoryItem.color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetBudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var budgetText by remember { mutableStateOf(currentBudget.toInt().toString()) }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Monthly Spending Budget",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter your maximum target spend per month. Naira Tracker will alert you when you exceed this limit.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*$"))) {
                            budgetText = it
                            hasError = false
                        }
                    },
                    label = { Text("Monthly Budget (₦)") },
                    leadingIcon = {
                        Text(
                            text = "₦",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenPrimary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    isError = hasError,
                    supportingText = if (hasError) {
                        { Text("Please enter a valid amount") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_amount_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(100000, 200000, 350000, 500000).forEach { preset ->
                        OutlinedButton(
                            onClick = { budgetText = preset.toString() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${preset / 1000}k",
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = budgetText.toDoubleOrNull()
                    if (parsed != null && parsed > 0) {
                        onConfirm(parsed)
                    } else {
                        hasError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                modifier = Modifier.testTag("confirm_budget_button")
            ) {
                Text("Save Budget", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
