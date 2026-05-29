package com.alfan.fanllymusic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun BlurredBackground(albumArtUri: String?) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (albumArtUri != null) {
            Image(
                painter = rememberAsyncImagePainter(albumArtUri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(96.dp)
                    .graphicsLayer(alpha = 0.92f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.38f))
        )
    }
}
