package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBgDark
import com.example.ui.theme.AccentBgLight
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.IconInactiveDark
import com.example.ui.theme.IconInactiveLight
import com.example.ui.theme.NavBgDark
import com.example.ui.theme.NavBgLight
import com.example.ui.theme.NavBorderDark
import com.example.ui.theme.NavBorderLight

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun BespokeFloatingNavBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    activityBadgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val navBg = if (isDark) NavBgDark else NavBgLight
    val navBorder = if (isDark) NavBorderDark else NavBorderLight
    val accentBg = if (isDark) AccentBgDark else AccentBgLight
    val iconInactive = if (isDark) IconInactiveDark else IconInactiveLight

    val items = listOf(
        NavTabItem("Home", IosIcons.Scanner),
        NavTabItem("Studio", IosIcons.Studio),
        NavTabItem("Create", IosIcons.Add),
        NavTabItem("Activity", IosIcons.History, badgeCount = activityBadgeCount),
        NavTabItem("Profile", IosIcons.Contact)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Dock
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = AccentPrimary.copy(alpha = 0.18f),
                    ambientColor = Color.Black.copy(alpha = 0.08f)
                )
                .clip(RoundedCornerShape(36.dp))
                .background(navBg)
                .border(width = 1.dp, color = navBorder, shape = RoundedCornerShape(36.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val totalWidth = maxWidth
            val itemWidth = totalWidth / items.size

            // Sliding Spring Indicator
            val indicatorOffsetX by animateDpAsState(
                targetValue = itemWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = 0.64f, // Matches cubic-bezier(0.34, 1.56, 0.64, 1)
                    stiffness = 380f
                ),
                label = "indicator_anim"
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffsetX)
                    .width(itemWidth)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(accentBg)
            )

            // Nav Items Row
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    val interactionSource = remember { MutableInteractionSource() }

                    // Icon Y translation: lifts up when active
                    val iconOffsetY by animateDpAsState(
                        targetValue = if (isSelected) (-6).dp else 0.dp,
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
                        label = "icon_offset"
                    )

                    // Active label alpha & slide up
                    val labelAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        label = "label_alpha"
                    )
                    val labelOffsetY by animateDpAsState(
                        targetValue = if (isSelected) 0.dp else 8.dp,
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
                        label = "label_offset"
                    )

                    val iconTint = if (isSelected) AccentPrimary else iconInactive

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onTabSelected(index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Icon container with badge
                            Box(
                                modifier = Modifier.offset(y = iconOffsetY),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = iconTint,
                                    modifier = Modifier.size(26.dp)
                                )

                                // Activity Badge
                                if (item.badgeCount > 0 && index == 3) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 8.dp, y = (-4).dp)
                                            .clip(CircleShape)
                                            .background(BadgeRed)
                                            .border(1.5.dp, navBg, CircleShape)
                                            .padding(horizontal = 4.dp, vertical = 1.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            // Active Label
                            if (labelAlpha > 0.05f) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .alpha(labelAlpha)
                                        .offset(y = labelOffsetY)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
