package com.powerfulboy.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powerfulboy.app.ui.theme.*

@Composable
fun MacroRing(
    label: String,
    current: Float,
    target: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (target > 0) (current / target).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(600),
        label = "macroRing"
    )

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(72.dp),
                color = color.copy(alpha = 0.15f),
                strokeWidth = 6.dp,
                strokeCap = StrokeCap.Round
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(72.dp),
                color = color,
                strokeWidth = 6.dp,
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = current.toInt().toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Text(
                    text = "/${target.toInt()}",
                    fontSize = 10.sp,
                    color = OnSurfaceMuted
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = OnSurfaceMuted)
    }
}

@Composable
fun MacroBar(
    label: String,
    current: Float,
    target: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (target > 0) (current / target).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(500),
        label = "macroBar"
    )
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 13.sp, color = OnSurface)
            Text(
                "${current.toInt()}g / ${target.toInt()}g",
                fontSize = 12.sp,
                color = OnSurfaceMuted
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun LimeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Lime,
            contentColor = Color(0xFF1A1A00)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OutlinedLimeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Lime),
        border = androidx.compose.foundation.BorderStroke(1.dp, Lime),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun PbTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions =
        androidx.compose.foundation.text.KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = OnSurfaceMuted) },
        modifier = modifier,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = OnBackground,
            unfocusedTextColor = OnBackground,
            focusedBorderColor = Lime,
            unfocusedBorderColor = Color(0xFF333333),
            cursorColor = Lime,
            focusedLabelColor = Lime
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = OnSurfaceMuted, fontSize = 15.sp)
    }
}
