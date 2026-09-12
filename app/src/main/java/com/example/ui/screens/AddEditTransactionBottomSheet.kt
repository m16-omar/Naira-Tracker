package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryPresets
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.DateUtils
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionBottomSheet(
    isOpen: Boolean,
    editingTransaction: TransactionEntity?,
    preselectedCategory: String?,
    onDismiss: () -> Unit,
    onSave: (id: Long, type: TransactionType, amount: Double, category: String, note: String, dateMillis: Long) -> Unit
) {
    if (!isOpen) return

    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var transactionType by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.type ?: TransactionType.EXPENSE)
    }

    var amountText by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.let { "%.2f".format(it.amount).removeSuffix(".00") } ?: "")
    }

    var selectedCategory by remember(editingTransaction, preselectedCategory) {
        mutableStateOf(
            editingTransaction?.category
                ?: preselectedCategory
                ?: if (transactionType == TransactionType.EXPENSE) "Food" else "Salary"
        )
    }

    var noteText by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.note ?: "")
    }

    var selectedDateMillis by remember(editingTransaction) {
        mutableLongStateOf(editingTransaction?.dateMillis ?: System.currentTimeMillis())
    }

    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val categories = if (transactionType == TransactionType.EXPENSE) {
        CategoryPresets.expenseCategories
    } else {
        CategoryPresets.incomeCategories
    }

    // Ensure selectedCategory is valid when type flips
    LaunchedEffect(transactionType) {
        val valid = categories.any { it.name == selectedCategory }
        if (!valid && editingTransaction == null) {
            selectedCategory = categories.first().name
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_edit_transaction_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingTransaction != null) "Edit Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_sheet_button")) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle for "Income vs Expense"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Expense option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (transactionType == TransactionType.EXPENSE) ExpenseRed else Color.Transparent
                        )
                        .clickable { transactionType = TransactionType.EXPENSE }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (transactionType == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Income option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (transactionType == TransactionType.INCOME) IncomeGreen else Color.Transparent
                        )
                        .clickable { transactionType = TransactionType.INCOME }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (transactionType == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Input Field with Naira (₦) prefix
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    // Allow only digits and a single decimal dot
                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amountText = input
                        amountError = false
                    }
                },
                label = { Text("Amount (₦)") },
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Text(
                        text = "₦",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (transactionType == TransactionType.EXPENSE) ExpenseRed else IncomeGreen,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                isError = amountError,
                supportingText = if (amountError) {
                    { Text("Please enter a valid amount greater than 0") }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (transactionType == TransactionType.EXPENSE) ExpenseRed else IncomeGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Quick-tap chips for common categories
            Text(
                text = "Quick Select Category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat.name
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat.name },
                        label = { Text(cat.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else cat.color
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("quick_category_${cat.name.lowercase().replace('/', '_')}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Category Dropdown
            Text(
                text = "Category Dropdown",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                val currentCategoryItem = CategoryPresets.getCategoryItem(selectedCategory, transactionType)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .clickable { isCategoryDropdownExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(currentCategoryItem.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = currentCategoryItem.icon,
                                contentDescription = null,
                                tint = currentCategoryItem.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedCategory,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown Arrow",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = isCategoryDropdownExpanded,
                    onDismissRequest = { isCategoryDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = null,
                                        tint = cat.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = cat.name)
                                }
                            },
                            onClick = {
                                selectedCategory = cat.name
                                isCategoryDropdownExpanded = false
                            },
                            trailingIcon = if (selectedCategory == cat.name) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = EmeraldGreenPrimary
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Note (Optional)
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note (Optional)") },
                placeholder = {
                    Text(
                        if (transactionType == TransactionType.EXPENSE)
                            "e.g., Lunch at buka, fuel, or Uber"
                        else
                            "e.g., Monthly client retainer, freelance fee"
                    )
                },
                singleLine = false,
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldGreenPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Date Picker (Default today)
            Text(
                text = "Date",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val updatedCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                selectedDateMillis = updatedCal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("date_picker_trigger"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select Date",
                    tint = EmeraldGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = DateUtils.formatDateOnly(selectedDateMillis),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val parsedAmount = amountText.toDoubleOrNull()
                        if (parsedAmount == null || parsedAmount <= 0) {
                            amountError = true
                        } else {
                            onSave(
                                editingTransaction?.id ?: 0L,
                                transactionType,
                                parsedAmount,
                                selectedCategory,
                                noteText.trim(),
                                selectedDateMillis
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (transactionType == TransactionType.EXPENSE) ExpenseRed else EmeraldGreenPrimary
                    ),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("save_transaction_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (editingTransaction != null) "Update" else "Save " + if (transactionType == TransactionType.EXPENSE) "Expense" else "Income",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
