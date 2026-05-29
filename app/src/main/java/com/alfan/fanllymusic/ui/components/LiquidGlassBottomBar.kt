package com.alfan.fanllymusic.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfan.fanllymusic.ui.theme.AccentPink

// ─── Tweak parameters ────────────────────────────────────────────────────────
//  radius          → barShape / selectedShape RoundedCornerShape values
//  border alpha    → BORDER_ALPHA
//  background alpha→ BG_ALPHA
//  selected bg     → SELECTED_BG_ALPHA
//  selected tint   → AccentPink (set in caller or pass accentColor param)
//  unselected tint → UNSELECTED_ALPHA
//  elevation       → ELEVATION
// ─────────────────────────────────────────────────────────────────────────────

private val ELEVATION        = 16.dp
private val BG_ALPHA         = 0.10f
private val BORDER_ALPHA     = 0.16f
private val SELECTED_BG_ALPHA= 0.14f
private val UNSELECTED_ALPHA = 0.72f

private val barShape         = RoundedCornerShape(32.dp)
private val selectedShape    = RoundedCornerShape(22.dp)

/** Data model for a single bottom nav destination. */
data class LiquidGlassBottomBarItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Floating glassmorphism bottom navigation bar.
 *
 * Pure Compose implementation (no external shader library).
 * Semi-transparent capsule floats over content; safe on API 26+.
 *
 * @param items          Navigation destinations to display.
 * @param currentRoute   Currently active route — drives selected state.
 * @param onItemClick    Invoked with the route string when an item is tapped.
 * @param modifier       Applied to the outer container (not the capsule).
 * @param accentColor    Tint applied to the selected icon/label.
 */
@Composable
fun LiquidGlassBottomBar(
    items: List<LiquidGlassBottomBarItem>,
    currentRoute: String?,
    onItemClick: (route: String) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = AccentPink
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = ELEVATION,
                    shape = barShape,
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(barShape)
                .background(Color.White.copy(alpha = BG_ALPHA))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = BORDER_ALPHA),
                    shape = barShape
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                LiquidGlassBottomBarButton(
                    item = item,
                    selected = selected,
                    accentColor = accentColor,
                    onClick = { if (!selected) onItemClick(item.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassBottomBarButton(
    item: LiquidGlassBottomBarItem,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconSize by animateDpAsState(
        targetValue = if (selected) 24.dp else 22.dp,
        animationSpec = tween(200),
        label = "lg_icon_size_${item.route}"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) accentColor else Color.White.copy(alpha = UNSELECTED_ALPHA),
        animationSpec = tween(200),
        label = "lg_icon_color_${item.route}"
    )
    val itemBg by animateColorAsState(
        targetValue = if (selected) Color.White.copy(alpha = SELECTED_BG_ALPHA) else Color.Transparent,
        animationSpec = tween(200),
        label = "lg_item_bg_${item.route}"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(selectedShape)
            .background(itemBg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.label,
                color = iconColor,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}
