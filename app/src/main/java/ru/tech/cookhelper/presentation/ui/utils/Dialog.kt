package ru.tech.cookhelper.presentation.ui.utils

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Dialog(val icon: ImageVector = Icons.Default.Android) {

    object None : Dialog()

    object Exit : Dialog(icon = Icons.Outlined.ExitToApp)

    object AboutApp : Dialog(icon = Icons.Outlined.HelpOutline)

    object PickProducts : Dialog()

    object Logout : Dialog(icon = Icons.Outlined.Logout)

    class CategorySelection(
        val categories: List<String>,
        val selectedCategory: String,
        val onCategorySelected: (String) -> Unit
    ) : Dialog(icon = Icons.Outlined.Category)

    class LeaveUnsavedData(
        @StringRes val title: Int,
        @StringRes val message: Int,
        val onLeave: () -> Unit
    ) : Dialog(icon = Icons.Outlined.Save)
}
