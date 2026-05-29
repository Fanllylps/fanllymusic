package com.alfan.fanllymusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfan.fanllymusic.domain.model.AudioQuality

@Composable
fun QualityBadge(quality: AudioQuality) {
    val text = when (quality) {
        AudioQuality.HI_RES_LOSSLESS -> "Hi-Res Lossless"
        AudioQuality.LOSSLESS -> "Lossless"
        else -> return
    }
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.09f), RoundedCornerShape(7.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.58f),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
