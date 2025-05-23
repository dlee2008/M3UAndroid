package com.m3u.tv.screens.playlist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import com.m3u.core.foundation.components.CircularProgressIndicator
import com.m3u.data.database.model.Channel
import com.m3u.data.service.MediaCommand
import com.m3u.tv.screens.dashboard.rememberChildPadding
import com.m3u.tv.utils.LocalHelper
import kotlinx.coroutines.launch

object ChannelDetailScreen {
    const val ChannelIdBundleKey = "channelId"
}

@Composable
fun ChannelDetailScreen(
    navigateToChannel: () -> Unit,
    onBackPressed: () -> Unit,
    viewModel: ChannelDetailViewModel = hiltViewModel()
) {
    val helper = LocalHelper.current
    val coroutineScope = rememberCoroutineScope()
    val channel by viewModel.channel.collectAsStateWithLifecycle()

    when (val channel = channel) {
        null -> {
            CircularProgressIndicator()
        }
        else -> {
            Details(
                channel = channel,
                navigateToChannel = {
                    coroutineScope.launch {
                        helper.play(MediaCommand.Common(channel.id))
                    }
                    navigateToChannel()
                },
                updateFavorite = viewModel::updateFavorite,
                onBackPressed = onBackPressed,
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize()
            )
        }
    }
}

@Composable
private fun Details(
    channel: Channel?,
    navigateToChannel: () -> Unit,
    updateFavorite: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val childPadding = rememberChildPadding()

    BackHandler(onBack = onBackPressed)
    LazyColumn(
        contentPadding = PaddingValues(bottom = 135.dp),
        modifier = modifier,
    ) {
        if (channel != null) {
            item {
                ChannelDetail(
                    channel = channel,
                    navigateToChannel = navigateToChannel,
                    updateFavorite = updateFavorite
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = childPadding.start)
                    .padding(BottomDividerPadding)
                    .fillMaxWidth()
                    .height(1.dp)
                    .alpha(0.15f)
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = childPadding.start),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val itemModifier = Modifier.width(192.dp)

                TitleValueText(
                    modifier = itemModifier,
                    title = "LIVE",
                    value = "channel.status"
                )
            }
        }
    }
}

private val BottomDividerPadding = PaddingValues(vertical = 48.dp)
