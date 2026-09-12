package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CategoryAirtime
import com.example.ui.theme.CategoryBills
import com.example.ui.theme.CategoryBusiness
import com.example.ui.theme.CategoryFood
import com.example.ui.theme.CategoryOthers
import com.example.ui.theme.CategorySalary
import com.example.ui.theme.CategoryShopping
import com.example.ui.theme.CategoryTransport

data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

object CategoryPresets {
    val expenseCategories = listOf(
        CategoryItem("Food", Icons.Default.Fastfood, CategoryFood),
        CategoryItem("Transport", Icons.Default.DirectionsBus, CategoryTransport),
        CategoryItem("Airtime/Data", Icons.Default.Smartphone, CategoryAirtime),
        CategoryItem("Bills", Icons.Default.ReceiptLong, CategoryBills),
        CategoryItem("Shopping", Icons.Default.ShoppingBag, CategoryShopping),
        CategoryItem("Others", Icons.Default.Category, CategoryOthers)
    )

    val incomeCategories = listOf(
        CategoryItem("Salary", Icons.Default.AttachMoney, CategorySalary),
        CategoryItem("Business Sales", Icons.Default.BusinessCenter, CategoryBusiness),
        CategoryItem("Freelance", Icons.Default.Work, Color(0xFF0284C7)),
        CategoryItem("Investment", Icons.Default.Savings, Color(0xFF0D9488)),
        CategoryItem("Gift", Icons.Default.CardGiftcard, Color(0xFFD946EF)),
        CategoryItem("Others", Icons.Default.Category, CategoryOthers)
    )

    fun getCategoryItem(name: String, type: TransactionType = TransactionType.EXPENSE): CategoryItem {
        val list = if (type == TransactionType.EXPENSE) expenseCategories else incomeCategories
        return list.find { it.name.equals(name, ignoreCase = true) }
            ?: (expenseCategories + incomeCategories).find { it.name.equals(name, ignoreCase = true) }
            ?: CategoryItem(name, Icons.Default.Category, CategoryOthers)
    }
}
