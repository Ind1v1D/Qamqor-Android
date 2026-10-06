package com.example.qamqorapp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.qamqorapp.R
import com.example.qamqorapp.ui.components.StarOutlineIcon
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors

/** Routes. These are stable navigation ids — never localized. */
object Routes {
    const val SETUP = "setup"
    const val FEED = "feed"
    const val FAVORITES = "favorites"
    const val PROFILE = "profile"
    const val CREATE = "create"
    const val DETAIL = "detail/{postId}"

    fun detail(postId: Long) = "detail/$postId"
}

/**
 * The three destinations that own the bottom navigation. `detail` and `create`
 * are deliberately absent: in the concept they are full screens with no tab bar.
 *
 * @property selectedIcon the filled glyph shown when the tab is active.
 * @property unselectedIcon drawn when inactive. This is a composable rather than
 *   an [ImageVector] because `material-icons-core` ships no star *outline* — the
 *   ☆ of the favorites tab has to be [StarOutlineIcon].
 */
private class TopLevelTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: @Composable (tint: Color, modifier: Modifier) -> Unit,
)

private val tabs = listOf(
    TopLevelTab(Routes.FEED, R.string.tab_feed, Icons.Filled.Home) { tint, m ->
        Icon(Icons.Filled.Home, contentDescription = null, tint = tint, modifier = m)
    },
    TopLevelTab(Routes.FAVORITES, R.string.tab_favorites, Icons.Filled.Star) { tint, m ->
        StarOutlineIcon(tint = tint, modifier = m)
    },
    TopLevelTab(Routes.PROFILE, R.string.tab_profile, Icons.Filled.Person) { tint, m ->
        Icon(Icons.Filled.Person, contentDescription = null, tint = tint, modifier = m)
    },
)

/** True for the routes that show the tab bar (and, on the feed, the FAB). */
fun shouldShowBottomBar(route: String?): Boolean =
    route == Routes.FEED || route == Routes.FAVORITES || route == Routes.PROFILE

/**
 * 56 dp bottom navigation plus the device's navigation-bar inset, per the spec.
 *
 * The bar lives in the shell rather than in each screen, so its height is known
 * when the feed positions its FAB «16dp над нижней навигацией».
 */
@Composable
fun QamqorBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.panel)
            // `.tabbar{border-top:1px solid var(--line)}` — a single hairline over
            // the whole bar (inset included), not a full rect border.
            .drawBehind {
                drawLine(
                    color = colors.line,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(bottom = bottomInset)
            .height(Dimens.BottomBar.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            val tint = if (selected) colors.pine else colors.inkSoft

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onNavigate(tab.route) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = tab.selectedIcon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    tab.unselectedIcon(tint, Modifier.size(20.dp))
                }
                Text(
                    text = stringResource(tab.labelRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
                // The little underline under the active tab.
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.dp)
                        .background(if (selected) colors.pine else androidx.compose.ui.graphics.Color.Transparent),
                )
            }
        }
    }
}
