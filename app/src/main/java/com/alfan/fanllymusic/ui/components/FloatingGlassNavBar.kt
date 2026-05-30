package com.alfan.fanllymusic.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.alfan.fanllymusic.ui.theme.AccentPink

private val NAV_BAR_HEIGHT = 76.dp
private val NAV_BAR_RADIUS = 40.dp
private val NAV_BAR_HORIZONTAL_PADDING = 24.dp
private val NAV_BAR_VERTICAL_PADDING = 12.dp
private const val CONTAINER_ALPHA = 0.42f
private const val BORDER_ALPHA = 0.16f

private val INDICATOR_HEIGHT = 64.dp
private const val INDICATOR_WIDTH_RATIO = 1.04f
private val INDICATOR_MIN_WIDTH = 78.dp
private val INDICATOR_MAX_WIDTH = 104.dp

private val SELECTED_ICON_SIZE = 30.dp
private val UNSELECTED_ICON_SIZE = 24.dp

data class FloatingGlassNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun FloatingGlassNavBar(
    items: List<FloatingGlassNavItem>,
    selectedRoute: String?,
    onItemClick: (FloatingGlassNavItem) -> Unit,
    modifier: Modifier = Modifier,
    selectedIndex: Int = items.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0),
    accentColor: Color = AccentPink
) {
    if (items.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = NAV_BAR_HORIZONTAL_PADDING,
                vertical = NAV_BAR_VERTICAL_PADDING
            ),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(NAV_BAR_HEIGHT)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(NAV_BAR_RADIUS),
                    ambientColor = Color.Black.copy(alpha = 0.40f),
                    spotColor = Color.Black.copy(alpha = 0.50f)
                )
                .clip(RoundedCornerShape(NAV_BAR_RADIUS))
                .background(Color.Black.copy(alpha = CONTAINER_ALPHA))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = BORDER_ALPHA),
                    shape = RoundedCornerShape(NAV_BAR_RADIUS)
                )
        ) {
            val itemWidth = maxWidth / items.size
            val indicatorWidth = remember(itemWidth) {
                (itemWidth * INDICATOR_WIDTH_RATIO).coerceDp(
                    min = INDICATOR_MIN_WIDTH,
                    max = INDICATOR_MAX_WIDTH
                )
            }
            val selectedOffset by animateDpAsState(
                targetValue = itemWidth * selectedIndex + ((itemWidth - indicatorWidth) / 2f),
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "floating_nav_indicator_offset"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.035f))
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.9f)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.18f))
                    .zIndex(0.5f)
            )

            MovingGlassIndicator(
                xOffset = selectedOffset,
                width = indicatorWidth,
                accentColor = accentColor,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .zIndex(1f)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    FloatingGlassNavButton(
                        item = item,
                        selected = selected,
                        accentColor = accentColor,
                        onClick = {
                            if (!selected) onItemClick(item)
                        },
                        modifier = Modifier
                            .width(itemWidth)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun MovingGlassIndicator(
    xOffset: Dp,
    width: Dp,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset(x = xOffset)
            .width(width)
            .height(INDICATOR_HEIGHT)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = accentColor.copy(alpha = 0.18f),
                spotColor = accentColor.copy(alpha = 0.22f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.20f),
                shape = RoundedCornerShape(32.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.68f)
                .height(32.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(accentColor.copy(alpha = 0.18f))
        )
    }
}

@Composable
private fun FloatingGlassNavButton(
    item: FloatingGlassNavItem,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconSize by animateDpAsState(
        targetValue = if (selected) SELECTED_ICON_SIZE else UNSELECTED_ICON_SIZE,
        animationSpec = spring(
            dampingRatio = 0.86f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "floating_nav_icon_size_${item.route}"
    )
    val itemScale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "floating_nav_item_scale_${item.route}"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            accentColor
        } else {
            Color.White.copy(alpha = 0.72f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "floating_nav_content_color_${item.route}"
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.78f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "floating_nav_label_alpha_${item.route}"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = itemScale
                    scaleY = itemScale
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = item.label,
                color = contentColor.copy(alpha = labelAlpha),
                style = MaterialTheme.typography.labelSmall,
                fontSize = if (selected) 11.sp else 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun Dp.coerceDp(min: Dp, max: Dp): Dp {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}
