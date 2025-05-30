package com.m3u.smartphone.ui.material.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.m3u.core.util.basic.title
import com.m3u.data.database.model.Playlist
import com.m3u.data.database.model.Channel
import com.m3u.i18n.R.string
import androidx.compose.material3.IconButton
import com.m3u.smartphone.ui.material.model.LocalSpacing

@Immutable
sealed class MediaSheetValue {
    data class ForyouScreen(
        val playlist: Playlist? = null
    ) : MediaSheetValue()

    data class PlaylistScreen(
        val channel: Channel? = null
    ) : MediaSheetValue()

    data class FavoriteScreen(
        val channel: Channel? = null
    ) : MediaSheetValue()
}


@Composable
fun MediaSheet(
    value: MediaSheetValue,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onUnsubscribePlaylist: (Playlist) -> Unit = { noImpl() },
    onPlaylistConfiguration: (Playlist) -> Unit = { noImpl() },
    onFavoriteChannel: (Channel) -> Unit = { noImpl() },
    onHideChannel: (Channel) -> Unit = { noImpl() },
    onSaveChannelCover: (Channel) -> Unit = { noImpl() },
    onCreateShortcut: (Channel) -> Unit = { noImpl() }
) {
    val spacing = LocalSpacing.current
    val clipboardManager = LocalClipboardManager.current

    val sheetState = rememberModalBottomSheetState()
    val visible = when (value) {
        is MediaSheetValue.ForyouScreen -> value.playlist != null
        is MediaSheetValue.PlaylistScreen -> value.channel != null
        is MediaSheetValue.FavoriteScreen -> value.channel != null
    }
    BottomSheet(
        sheetState = sheetState,
        visible = visible,
        shouldDismissOnBackPress = false,
        header = {
            when (value) {
                is MediaSheetValue.ForyouScreen -> ForyouScreenMediaSheetHeaderImpl(
                    playlist = value.playlist,
                    onPlaylistConfiguration = onPlaylistConfiguration
                )

                is MediaSheetValue.PlaylistScreen -> PlaylistScreenMediaSheetHeaderImpl(
                    channel = value.channel
                )

                is MediaSheetValue.FavoriteScreen -> FavoriteScreenMediaSheetHeaderImpl(
                    channel = value.channel
                )
            }
        },
        body = {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.small),
                modifier = Modifier.padding(spacing.medium)
            ) {
                when (value) {
                    is MediaSheetValue.ForyouScreen -> {
                        value.playlist?.let { playlist ->
                            val playlistUrl = playlist.url
                            MediaSheetItem(
                                stringRes = string.feat_foryou_unsubscribe_playlist,
                                onClick = {
                                    onUnsubscribePlaylist(playlist)
                                    onDismissRequest()
                                }
                            )
                            MediaSheetItem(
                                stringRes = string.feat_foryou_copy_playlist_url,
                                onClick = {
                                    clipboardManager.setText(
                                        AnnotatedString(playlistUrl)
                                    )
                                    onDismissRequest()
                                }
                            )
                        }
                    }

                    is MediaSheetValue.PlaylistScreen -> {
                        value.channel?.let {
                            MediaSheetItem(
                                stringRes = if (!it.favourite) string.feat_playlist_dialog_favourite_title
                                else string.feat_playlist_dialog_favourite_cancel_title,
                                onClick = {
                                    onFavoriteChannel(it)
                                    onDismissRequest()
                                }
                            )
                            MediaSheetItem(
                                stringRes = string.feat_playlist_dialog_hide_title,
                                onClick = {
                                    onHideChannel(it)
                                    onDismissRequest()
                                }
                            )
                            MediaSheetItem(
                                stringRes = string.feat_playlist_dialog_create_shortcut_title,
                                onClick = {
                                    onCreateShortcut(it)
                                    onDismissRequest()
                                }
                            )
                            MediaSheetItem(
                                stringRes = string.feat_playlist_dialog_save_picture_title,
                                onClick = {
                                    onSaveChannelCover(it)
                                    onDismissRequest()
                                }
                            )
                        }
                    }

                    is MediaSheetValue.FavoriteScreen -> {
                        value.channel?.let {
                            MediaSheetItem(
                                stringRes = if (!it.favourite) string.feat_playlist_dialog_favourite_title
                                else string.feat_playlist_dialog_favourite_cancel_title,
                                onClick = {
                                    onFavoriteChannel(it)
                                    onDismissRequest()
                                }
                            )
                            MediaSheetItem(
                                stringRes = string.feat_playlist_dialog_create_shortcut_title,
                                onClick = {
                                    onCreateShortcut(it)
                                    onDismissRequest()
                                }
                            )
                        }
                    }
                }
            }
        },
        onDismissRequest = onDismissRequest,
        modifier = modifier
    )
}

@Composable
private fun RowScope.ForyouScreenMediaSheetHeaderImpl(
    playlist: Playlist?,
    onPlaylistConfiguration: (Playlist) -> Unit,
) {
    val spacing = LocalSpacing.current
    playlist?.let {
        Row {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = it.title,
                    style = MaterialTheme.typography.titleLarge
                )
                it.userAgent?.ifEmpty { null }?.let { ua ->
                    Text(
                        text = ua,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalContentColor.current.copy(0.38f),
                        maxLines = 1,
                        fontFamily = FontFamilies.LexendExa,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = { onPlaylistConfiguration(playlist) }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun RowScope.PlaylistScreenMediaSheetHeaderImpl(
    channel: Channel?
) {
    channel?.let {
        Text(
            text = it.title,
            style = MaterialTheme.typography.titleLarge
        )
    }
    Spacer(modifier = Modifier.weight(1f))
}

@Composable
private fun RowScope.FavoriteScreenMediaSheetHeaderImpl(
    channel: Channel?
) {
    channel?.let {
        Text(
            text = it.title,
            style = MaterialTheme.typography.titleLarge
        )
    }
    Spacer(modifier = Modifier.weight(1f))
}

@Composable
private fun MediaSheetItem(
    @StringRes stringRes: Int,
    onClick: () -> Unit
) {
    OutlinedCard {
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(stringRes).title(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            modifier = Modifier.clickable { onClick() }
        )
    }
}

private fun noImpl(): Nothing =
    throw NotImplementedError("A Media Sheet operation is not implemented")