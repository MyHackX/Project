package com.mtl.My_Hack_X.components

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

/**
 * A reusable date picker component
 *
 * @param selectedDate The currently selected date
 * @param onDateSelected Callback for when a date is selected
 * @param label Optional label for the date picker
 * @param modifier Modifier for the date picker
 * @param formatPattern Date format pattern (default: "yyyy-MM-dd")
 * @param placeholder Optional placeholder text when no date is selected
 */
@Composable
fun DatePicker(
    selectedDate: Date?,
    onDateSelected: (Date) -> Unit,
    label: String = "Date",
    modifier: Modifier = Modifier,
    formatPattern: String = "yyyy-MM-dd",
    placeholder: String = "Select date"
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat(formatPattern, Locale.getDefault())
    
    // Set calendar to selected date or current date
    selectedDate?.let {
        calendar.time = it
    }
    
    // Get current values for date picker
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    
    // Create date picker dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            onDateSelected(calendar.time)
        },
        year, month, day
    )
    
    // Format displayed date
    val displayedDate = selectedDate?.let { dateFormat.format(it) } ?: placeholder
    
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select date",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = displayedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * A simpler inline date picker component that looks like a button
 */
@Composable
fun DatePickerButton(
    selectedDate: Date?,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier,
    formatPattern: String = "MMM dd, yyyy"
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat(formatPattern, Locale.getDefault())
    
    // Set calendar to selected date or current date
    selectedDate?.let {
        calendar.time = it
    }
    
    // Get current values for date picker
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    
    // Create date picker dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            onDateSelected(calendar.time)
        },
        year, month, day
    )
    
    // Format displayed date
    val displayedDate = selectedDate?.let { dateFormat.format(it) } ?: "Select date"
    
    OutlinedButton(
        onClick = { datePickerDialog.show() },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = "Select date",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(displayedDate)
    }
} 