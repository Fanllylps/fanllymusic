package com.alfan.fanllymusic

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.alfan.fanllymusic.ui.navigation.FanllyNavGraph
import com.alfan.fanllymusic.ui.theme.MyComposeApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        setContent {
            MyComposeApplicationTheme {
                FanllyNavGraph()
            }
        }
    }
}
