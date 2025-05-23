package com.m3u.smartphone.ui.business.channel

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ScreenRotationAlt
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import androidx.media3.common.Player
import com.m3u.business.channel.PlayerState
import com.m3u.core.architecture.preferences.PreferencesKeys
import com.m3u.core.architecture.preferences.preferenceOf
import com.m3u.core.foundation.suggest.any
import com.m3u.core.foundation.suggest.suggestAll
import com.m3u.core.foundation.ui.composableOf
import com.m3u.core.foundation.ui.thenIf
import com.m3u.core.util.basic.isNotEmpty
import com.m3u.data.database.model.AdjacentChannels
import com.m3u.i18n.R.string
import com.m3u.smartphone.ui.business.channel.components.Paddings
import com.m3u.smartphone.ui.business.channel.components.MaskTextButton
import com.m3u.smartphone.ui.business.channel.components.PlayerMask
import com.m3u.smartphone.ui.common.helper.LocalHelper
import com.m3u.smartphone.ui.material.components.FontFamilies
import com.m3u.smartphone.ui.material.components.mask.MaskButton
import com.m3u.smartphone.ui.material.components.mask.MaskCircleButton
import com.m3u.smartphone.ui.material.components.mask.MaskPanel
import com.m3u.smartphone.ui.material.components.mask.MaskState
import com.m3u.smartphone.ui.material.effects.currentBackStackEntry
import com.m3u.smartphone.ui.material.model.LocalSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun ChannelMask(
    adjacentChannels: AdjacentChannels?,
    title: String,
    gesture: MaskGesture?,
    playlistTitle: String,
    playerState: PlayerState,
    volume: Float,
    brightness: Float,
    maskState: MaskState,
    favourite: Boolean,
    isSeriesPlaylist: Boolean,
    isPanelExpanded: Boolean,
    useVertical: Boolean,
    hasTrack: Boolean,
    cwPosition: Long,
    onResetPlayback: () -> Unit,
    onSpeedUpdated: (Float) -> Unit,
    onSpeedStart: () -> Unit,
    onSpeedEnd: () -> Unit,
    onFavorite: () -> Unit,
    openDlnaDevices: () -> Unit,
    openChooseFormat: () -> Unit,
    openOrClosePanel: () -> Unit,
    onEnterPipMode: () -> Unit,
    onVolume: (Float) -> Unit,
    onNextChannelClick: () -> Unit,
    onPreviousChannelClick: () -> Unit,
    onPaddingsChanged: (Paddings) -> Unit,
    modifier: Modifier = Modifier,
) {
    val helper = LocalHelper.current
    val spacing = LocalSpacing.current
    val coroutineScope = rememberCoroutineScope()

    val onBackPressedDispatcher = checkNotNull(
        LocalOnBackPressedDispatcherOwner.current
    ).onBackPressedDispatcher

    // because they will be updated frequently,
    // they must be wrapped with rememberUpdatedState when using them.
    val currentVolume by rememberUpdatedState(volume)
    val currentBrightness by rememberUpdatedState(brightness)
    val currentUseVertical by rememberUpdatedState(useVertical)
    val currentIsPanelExpanded by rememberUpdatedState(isPanelExpanded)
    val currentCwPosition by rememberUpdatedState(cwPosition)

    val muted = currentVolume == 0f

    val defaultBrightnessOrVolumeContentDescription = when {
        muted -> stringResource(string.feat_channel_tooltip_unmute)
        else -> stringResource(string.feat_channel_tooltip_mute)
    }
    val brightnessOrVolumeText by remember {
        derivedStateOf {
            when (gesture) {
                MaskGesture.VOLUME -> "${(currentVolume.coerceIn(0f..1f) * 100).roundToInt()}%"
                MaskGesture.BRIGHTNESS -> "${(currentBrightness.coerceIn(0f..1f) * 100).roundToInt()}%"
                else -> null
            }
        }
    }

    val slider by preferenceOf(PreferencesKeys.SLIDER)
    val screencast by preferenceOf(PreferencesKeys.SCREENCAST)
    val alwaysShowReplay by preferenceOf(PreferencesKeys.ALWAYS_SHOW_REPLAY)
    val screenRotating by preferenceOf(PreferencesKeys.SCREEN_ROTATING)

    val isStaticAndSeekable by remember(
        playerState.player,
        playerState.playState
    ) {
        derivedStateOf {
            val currentPlayer = playerState.player
            when {
                currentPlayer == null -> false
                !currentPlayer.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM) -> false
                else -> with(currentPlayer) {
                    !isCurrentMediaItemDynamic && isCurrentMediaItemSeekable
                }
            }
        }
    }
    val isSpeedable by remember(
        playerState.player,
        playerState.playState
    ) {
        derivedStateOf {
            playerState.player
                ?.isCommandAvailable(Player.COMMAND_SET_SPEED_AND_PITCH) == true
        }
    }

    val contentPosition by produceState(
        initialValue = -1L,
        isStaticAndSeekable,
        slider
    ) {
        while (slider && isStaticAndSeekable) {
            delay(50.milliseconds)
            value = playerState.player?.currentPosition ?: -1L
        }
        value = -1L
    }
    val contentDuration by produceState(
        initialValue = -1L,
        isStaticAndSeekable,
        slider
    ) {
        while (slider && isStaticAndSeekable) {
            delay(50.milliseconds)
            value = playerState.player?.duration?.absoluteValue ?: -1L
        }
        value = -1L
    }

    var volumeBeforeMuted: Float by remember { mutableFloatStateOf(0.4f) }

    var bufferedPosition: Long? by remember { mutableStateOf(null) }
    LaunchedEffect(bufferedPosition) {
        bufferedPosition?.let {
            delay(800.milliseconds)
            playerState.player?.seekTo(it)
        }
    }
    LaunchedEffect(playerState.playState) {
        if (playerState.playState == Player.STATE_READY) {
            bufferedPosition = null
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        MaskPanel(
            state = maskState,
            isSpeedGestureEnabled = isSpeedable,
            onSpeedUpdated = onSpeedUpdated,
            onSpeedStart = onSpeedStart,
            onSpeedEnd = onSpeedEnd,
            modifier = Modifier.align(Alignment.Center)
        )
        val color by animateColorAsState(
            Color.Black.copy(alpha = if (currentIsPanelExpanded) 0f else 0.54f)
        )
        val playStateDisplayText = ChannelMaskUtils.playStateDisplayText(playerState.playState)
        val exceptionDisplayText =
            ChannelMaskUtils.playbackExceptionDisplayText(playerState.playerError)

        val cwPositionObj = run {
            currentCwPosition.takeIf {
                currentUseVertical && !currentIsPanelExpanded && playerState.playState == Player.STATE_READY
                        && it != -1L
            }?.let { CwPosition(it) }
        }

        PlayerMask(
            state = maskState,
            color = color,
            header = {
                val backStackEntry by currentBackStackEntry()
                MaskButton(
                    state = maskState,
                    icon = backStackEntry?.navigationIcon ?: Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = { onBackPressedDispatcher.onBackPressed() },
                    contentDescription = stringResource(string.feat_channel_tooltip_on_back_pressed)
                )
                Spacer(modifier = Modifier.weight(1f))

                MaskTextButton(
                    state = maskState,
                    icon = when {
                        volume == 0f -> Icons.AutoMirrored.Rounded.VolumeOff
                        else -> Icons.AutoMirrored.Rounded.VolumeUp
                    },
                    text = brightnessOrVolumeText,
                    tint = if (muted) MaterialTheme.colorScheme.error else Color.Unspecified,
                    onClick = {
                        onVolume(
                            if (volume != 0f) {
                                volumeBeforeMuted = volume
                                0f
                            } else volumeBeforeMuted
                        )
                    },
                    contentDescription = defaultBrightnessOrVolumeContentDescription
                )
                if (!isSeriesPlaylist) {
                    MaskButton(
                        state = maskState,
                        icon = Icons.Rounded.Star,
                        tint = if (favourite) Color(0xffffcd3c) else Color.Unspecified,
                        onClick = onFavorite,
                        contentDescription = if (favourite) stringResource(string.feat_channel_tooltip_unfavourite)
                        else stringResource(string.feat_channel_tooltip_favourite)
                    )
                }

                if (hasTrack) {
                    MaskButton(
                        state = maskState,
                        icon = Icons.Rounded.HighQuality,
                        onClick = openChooseFormat,
                        contentDescription = stringResource(string.feat_channel_tooltip_choose_format)
                    )
                }

                if (!currentUseVertical) {
                    MaskButton(
                        state = maskState,
                        icon = if (currentIsPanelExpanded) Icons.Rounded.Archive
                        else Icons.Rounded.Unarchive,
                        onClick = openOrClosePanel,
                        contentDescription = stringResource(string.feat_channel_tooltip_open_panel)
                    )
                }

                if (screencast) {
                    MaskButton(
                        state = maskState,
                        icon = Icons.Rounded.Cast,
                        onClick = openDlnaDevices,
                        contentDescription = stringResource(string.feat_channel_tooltip_cast)
                    )
                }
                if (playerState.videoSize.isNotEmpty) {
                    MaskButton(
                        state = maskState,
                        icon = Icons.Rounded.PictureInPicture,
                        onClick = onEnterPipMode,
                        contentDescription = stringResource(string.feat_channel_tooltip_enter_pip_mode),
                        wakeWhenClicked = false
                    )
                }
            },
            body = {
                val centerRole = MaskCenterRole.of(
                    playerState.playState,
                    playerState.isPlaying,
                    alwaysShowReplay,
                    playerState.playerError
                )
                Box(Modifier.size(36.dp)) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !currentIsPanelExpanded && adjacentChannels?.prevId != null,
                        enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 6 }),
                        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it / 6 }),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        MaskNavigateButton(
                            state = maskState,
                            navigateRole = MaskNavigateRole.Previous,
                            onClick = onPreviousChannelClick,
                        )
                    }
                }

                Box(Modifier.size(52.dp)) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !currentIsPanelExpanded && centerRole != MaskCenterRole.Loading,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        MaskCenterButton(
                            state = maskState,
                            centerRole = centerRole,
                            onPlay = { playerState.player?.play() },
                            onPause = { playerState.player?.pause() },
                            onRetry = { coroutineScope.launch { helper.replay() } },
                        )
                    }
                }
                Box(Modifier.size(36.dp)) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !currentIsPanelExpanded && adjacentChannels?.nextId != null,
                        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 6 }),
                        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 6 }),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        MaskNavigateButton(
                            state = maskState,
                            navigateRole = MaskNavigateRole.Next,
                            onClick = onNextChannelClick,
                        )
                    }
                }
            },
            control = {
                Crossfade(
                    targetState = cwPositionObj,
                    modifier = Modifier.align { size: IntSize, space: IntSize, _: LayoutDirection ->
                        val centerX = (space.width - size.width).toFloat() / 2f
                        val centerY = (space.height - size.height).toFloat() / 2f
                        val x = centerX
                        val y = centerY + playerState.videoSize.height() / 2
                        IntOffset(x.fastRoundToInt(), y.fastRoundToInt())
                    }
                ) {
                    if (it != null) {
                        CwPositionSliderImpl(
                            position = it.milliseconds,
                            onResetPlayback = onResetPlayback
                        )
                    }
                }
            },
            footer = composableOf<RowScope>(
                any {
                    suggest { !currentIsPanelExpanded }
                    suggest { !currentUseVertical }
                    suggest { playStateDisplayText.isNotEmpty() }
                    suggest { exceptionDisplayText.isNotEmpty() }
                    suggestAll {
                        suggest { isStaticAndSeekable }
                        suggest { slider }
                    }
                }
            ) {
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .semantics(mergeDescendants = true) { }
                        .weight(1f)
                        .padding(bottom = spacing.small)
                ) {
                    val alpha by animateFloatAsState(
                        if (!currentIsPanelExpanded || !currentUseVertical) 1f else 0f
                    )
                    Column(Modifier.alpha(alpha)) {
                        Text(
                            text = playlistTitle.trim().uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            color = LocalContentColor.current.copy(0.54f),
                            fontFamily = FontFamilies.LexendExa,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.basicMarquee()
                        )
                        Text(
                            text = title.trim(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                    if (playStateDisplayText.isNotEmpty()
                        || exceptionDisplayText.isNotEmpty()
                        || (isStaticAndSeekable && slider)
                    ) {
                        Spacer(
                            modifier = Modifier.height(spacing.small)
                        )
                    }
                    if (playStateDisplayText.isNotEmpty()) {
                        Text(
                            text = playStateDisplayText.uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalContentColor.current.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                    if (exceptionDisplayText.isNotBlank()) {
                        Text(
                            text = exceptionDisplayText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }
                val autoRotating by ChannelMaskUtils.IsAutoRotatingEnabled
                LaunchedEffect(autoRotating) {
                    if (autoRotating) {
                        helper.screenOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }
                if (screenRotating && !autoRotating) {
                    MaskButton(
                        state = maskState,
                        icon = Icons.Rounded.ScreenRotationAlt,
                        onClick = {
                            helper.screenOrientation = when (helper.screenOrientation) {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            }
                        },
                        contentDescription = stringResource(string.feat_channel_tooltip_screen_rotating)
                    )
                }
            },
            slider = composableOf(slider && isStaticAndSeekable) {
                SliderImpl(
                    contentDuration = contentDuration,
                    contentPosition = contentPosition,
                    bufferedPosition = bufferedPosition,
                    isPanelExpanded = currentIsPanelExpanded,
                    onBufferedPositionChanged = {
                        bufferedPosition = it
                        maskState.wake()
                    }
                )
            },
            onPaddingsChanged = onPaddingsChanged,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun SliderImpl(
    bufferedPosition: Long?,
    onBufferedPositionChanged: (Long) -> Unit,
    contentPosition: Long,
    contentDuration: Long,
    isPanelExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val animContentPosition by animateFloatAsState(
        targetValue = (bufferedPosition
            ?: contentPosition.coerceAtLeast(0L)).toFloat(),
        label = "anim-content-position"
    )
    val fontWeight by animateIntAsState(
        targetValue = if (bufferedPosition != null) 800
        else 400,
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = ChannelMaskUtils.timeunitDisplayTest(
                (bufferedPosition ?: contentPosition)
                    .toDuration(DurationUnit.MILLISECONDS)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalContentColor.current.copy(alpha = 0.75f),
            maxLines = 1,
            fontFamily = FontFamilies.JetbrainsMono,
            fontSize = 12.sp,
            fontWeight = FontWeight(fontWeight),
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.basicMarquee()
        )
        val sliderThumbWidthDp by animateDpAsState(4.dp)
        val sliderInteractionSource = remember { MutableInteractionSource() }
        Slider(
            value = animContentPosition,
            valueRange = 0f..contentDuration
                .coerceAtLeast(0L)
                .toFloat(),
            onValueChange = { onBufferedPositionChanged(it.roundToLong()) },
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = sliderInteractionSource,
                    thumbSize = DpSize(sliderThumbWidthDp, 20.dp)
                )
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CwPositionSliderImpl(
    position: Long,
    modifier: Modifier = Modifier,
    onResetPlayback: () -> Unit
) {
    val spacing = LocalSpacing.current
    val time = remember(position) {
        position.toDuration(DurationUnit.MILLISECONDS).toComponents { h, m, s, _ ->
            listOf(h, m, s)
                .dropWhile { it.toInt() == 0 }
                .joinToString(":") {
                    if (it.toInt() >= 10) it.toString()
                    else "0$it"
                }
        }
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
//        Text(
//            text = stringResource(string.feat_channel_cw_position_title, time),
//            modifier = Modifier.weight(1f)
//        )
        Text(
            text = buildAnnotatedString {
                withLink(
                    LinkAnnotation.Clickable(
                        tag = stringResource(string.feat_channel_cw_position_button),
                        linkInteractionListener = { onResetPlayback() }
                    ),
                ) {
                    append(stringResource(string.feat_channel_cw_position_button))
                }
            },
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = LocalContentColor.current.copy(0.65f),
                    shape = CircleShape
                )
                .padding(spacing.small)
        )
    }
}

@Composable
private fun MaskCenterButton(
    state: MaskState,
    centerRole: MaskCenterRole,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onRetry: () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val isHovered by interactionSource.collectIsHoveredAsState()
        val isDragged by interactionSource.collectIsDraggedAsState()
        val isScaled by remember {
            derivedStateOf { isPressed || isHovered || isDragged }
        }
        val scale by animateFloatAsState(
            targetValue = if (isScaled) 0.65f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        MaskCircleButton(
            state = state,
            interactionSource = interactionSource,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            icon = when (centerRole) {
                MaskCenterRole.Play -> Icons.Rounded.PlayArrow
                MaskCenterRole.Pause -> Icons.Rounded.Pause
                else -> Icons.Rounded.Refresh
            },
            onClick = when (centerRole) {
                MaskCenterRole.Replay -> onRetry
                MaskCenterRole.Play -> onPlay
                MaskCenterRole.Pause -> onPause
                else -> {
                    {}
                }
            }
        )
    }
}

@Composable
private fun MaskNavigateButton(
    state: MaskState,
    navigateRole: MaskNavigateRole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val isHovered by interactionSource.collectIsHoveredAsState()
        val isDragged by interactionSource.collectIsDraggedAsState()
        val isScaled by remember {
            derivedStateOf { isPressed || isHovered || isDragged }
        }
        val scale by animateFloatAsState(if (isScaled) 0.85f else 1f)
        MaskCircleButton(
            state = state,
            isSmallDimension = true,
            icon = when (navigateRole) {
                MaskNavigateRole.Next -> Icons.Rounded.SkipNext
                MaskNavigateRole.Previous -> Icons.Rounded.SkipPrevious
            },
            interactionSource = interactionSource,
            onClick = onClick,
            modifier = Modifier
                .thenIf(!enabled) { Modifier.alpha(0f) }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}

private enum class MaskCenterRole {
    Replay, Play, Pause, Loading;

    companion object {
        @Composable
        fun of(
            @Player.State playState: Int,
            isPlaying: Boolean,
            alwaysShowReplay: Boolean,
            playerError: Exception?,
        ): MaskCenterRole = remember(playState, alwaysShowReplay, playerError, isPlaying) {
            when {
                playState == Player.STATE_BUFFERING -> Loading
                alwaysShowReplay || playState in arrayOf(
                    Player.STATE_IDLE,
                    Player.STATE_ENDED
                ) || playerError != null -> Replay

                else -> if (!isPlaying) Play else Pause
            }
        }
    }
}

private enum class MaskNavigateRole {
    Next, Previous
}

data class CwPosition(val milliseconds: Long)
