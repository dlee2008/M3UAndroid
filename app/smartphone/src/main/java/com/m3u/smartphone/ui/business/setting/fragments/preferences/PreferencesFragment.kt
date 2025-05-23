package com.m3u.smartphone.ui.business.setting.fragments.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.m3u.core.unit.DataUnit
import com.m3u.smartphone.ui.material.ktx.plus
import com.m3u.smartphone.ui.material.model.LocalSpacing
import com.m3u.smartphone.ui.material.components.SettingDestination

@Composable
internal fun PreferencesFragment(
    fragment: SettingDestination,
    contentPadding: PaddingValues,
    versionName: String,
    versionCode: Int,
    navigateToPlaylistManagement: () -> Unit,
    navigateToThemeSelector: () -> Unit,
    navigateToOptional: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding + PaddingValues(spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.medium)
    ) {
        item {
            RegularPreferences(
                fragment = fragment,
                navigateToPlaylistManagement = navigateToPlaylistManagement,
                navigateToThemeSelector = navigateToThemeSelector,
                navigateToOptional = navigateToOptional
            )
        }
        item {
            OtherPreferences(
                versionName = versionName,
                versionCode = versionCode,
            )
        }
    }
}
