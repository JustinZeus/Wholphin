package com.github.damontecres.wholphin.ui.preferences.displaysize

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.github.damontecres.wholphin.R
import com.github.damontecres.wholphin.data.model.BaseItem
import com.github.damontecres.wholphin.preferences.AppPreference
import com.github.damontecres.wholphin.preferences.AppPreferences
import com.github.damontecres.wholphin.preferences.AppSliderPreference
import com.github.damontecres.wholphin.preferences.updateInterfacePreferences
import com.github.damontecres.wholphin.services.BackdropService
import com.github.damontecres.wholphin.ui.FontAwesome
import com.github.damontecres.wholphin.ui.data.RowColumn
import com.github.damontecres.wholphin.ui.launchIO
import com.github.damontecres.wholphin.ui.main.HomePageContent
import com.github.damontecres.wholphin.ui.main.settings.HomeSettingsListItem
import com.github.damontecres.wholphin.ui.main.settings.TitleText
import com.github.damontecres.wholphin.ui.main.settings.settingsWidth
import com.github.damontecres.wholphin.ui.preferences.PreferencesViewModel
import com.github.damontecres.wholphin.ui.preferences.SliderPreference
import com.github.damontecres.wholphin.ui.tryRequestFocus
import com.github.damontecres.wholphin.ui.util.ResStringProvider
import com.github.damontecres.wholphin.ui.util.withinBoundsOrDefault
import com.github.damontecres.wholphin.util.HomeRowLoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder
import org.jellyfin.sdk.model.api.request.GetItemsRequest
import timber.log.Timber
import javax.inject.Inject

private const val PREVIEW_POSTER_COUNT = 5
private const val PREVIEW_EPISODE_COUNT = 4
private val PREVIEW_NAV_RAIL_WIDTH_DP = 64.dp
private val PREVIEW_NAV_ITEM_HEIGHT_DP = 56.dp
private val PREVIEW_NAV_ICON_SIZE_DP = 24.dp

private val PreviewNavRailIcons: List<ImageVector> =
    listOf(
        Icons.Default.Person,
        Icons.Default.Home,
        Icons.Default.Favorite,
        Icons.Default.Search,
        Icons.Default.Add,
        Icons.Default.Star,
        Icons.Default.PlayArrow,
        Icons.Default.ArrowDropDown,
        Icons.Default.Settings,
    )

@HiltViewModel
class DisplaySizeViewModel
    @Inject
    constructor(
        private val api: ApiClient,
        private val backdropService: BackdropService,
    ) : ViewModel() {
        val posters = MutableStateFlow<List<BaseItem>>(emptyList())
        val episodes = MutableStateFlow<List<BaseItem>>(emptyList())

        fun updateBackdrop(item: BaseItem) {
            viewModelScope.launchIO { backdropService.submit(item) }
        }

        init {
            viewModelScope.launchIO {
                fetch(BaseItemKind.MOVIE, PREVIEW_POSTER_COUNT, posters)
            }
            viewModelScope.launchIO {
                fetch(BaseItemKind.EPISODE, PREVIEW_EPISODE_COUNT, episodes)
            }
        }

        private suspend fun fetch(
            kind: BaseItemKind,
            limit: Int,
            target: MutableStateFlow<List<BaseItem>>,
        ) {
            try {
                val request =
                    GetItemsRequest(
                        recursive = true,
                        includeItemTypes = listOf(kind),
                        sortBy = listOf(ItemSortBy.DATE_CREATED),
                        sortOrder = listOf(SortOrder.DESCENDING),
                        limit = limit,
                    )
                val result = api.itemsApi.getItems(request).content
                val items = (result.items ?: emptyList()).map { BaseItem.from(it, api, false) }
                target.value = items
            } catch (ex: Exception) {
                Timber.w(ex, "DisplaySize preview: could not fetch %s sample", kind)
            }
        }
    }

@Composable
fun DisplaySizePage(
    initialPreferences: AppPreferences,
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel(),
    previewViewModel: DisplaySizeViewModel = hiltViewModel(),
) {
    var preferences by remember { mutableStateOf(initialPreferences) }
    LaunchedEffect(Unit) {
        viewModel.preferenceDataStore.data.collect { preferences = it }
    }
    val scope = rememberCoroutineScope()
    val iface = preferences.interfacePreferences

    val uiScale = iface.uiScalePercent.withinBoundsOrDefault(AppPreference.UiScale)
    val cardSize = iface.cardSizePercent.withinBoundsOrDefault(AppPreference.CardSize)
    val spacing = iface.spacingPercent.withinBoundsOrDefault(AppPreference.Spacing)

    val posters by previewViewModel.posters.collectAsState()
    val episodes by previewViewModel.episodes.collectAsState()

    // Freezes the rail width and slider labels so they don't reflow under the user's finger
    // while dragging UI scale.
    val ambientDensity = LocalDensity.current
    val pageDensity = remember { ambientDensity }

    val context = LocalContext.current
    val systemDensity =
        remember {
            val dm = context.resources.displayMetrics
            Density(
                density = dm.density,
                fontScale = context.resources.configuration.fontScale,
            )
        }
    val previewDensity =
        remember(systemDensity, uiScale) {
            val factor = uiScale / 100f
            Density(
                density = systemDensity.density * factor,
                fontScale = systemDensity.fontScale,
            )
        }

    CompositionLocalProvider(LocalDensity provides pageDensity) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxSize(),
        ) {
            Box(
                modifier =
                    Modifier
                        .width(settingsWidth)
                        .fillMaxHeight()
                        .background(color = MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
            ) {
                DisplaySizeSettingsColumn(
                    uiScale = uiScale,
                    cardSize = cardSize,
                    spacing = spacing,
                    onUiScaleChange = { value ->
                        scope.launch {
                            viewModel.preferenceDataStore.updateData {
                                it.updateInterfacePreferences { uiScalePercent = value }
                            }
                        }
                    },
                    onCardSizeChange = { value ->
                        scope.launch {
                            viewModel.preferenceDataStore.updateData {
                                it.updateInterfacePreferences { cardSizePercent = value }
                            }
                        }
                    },
                    onSpacingChange = { value ->
                        scope.launch {
                            viewModel.preferenceDataStore.updateData {
                                it.updateInterfacePreferences { spacingPercent = value }
                            }
                        }
                    },
                    onClickReset = {
                        scope.launch {
                            viewModel.preferenceDataStore.updateData {
                                it.updateInterfacePreferences {
                                    uiScalePercent = AppPreference.UiScale.defaultValue.toInt()
                                    cardSizePercent = AppPreference.CardSize.defaultValue.toInt()
                                    spacingPercent = AppPreference.Spacing.defaultValue.toInt()
                                }
                            }
                        }
                    },
                )
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .weight(1f),
            ) {
                CompositionLocalProvider(LocalDensity provides previewDensity) {
                    DisplaySizePreview(
                        posters = posters,
                        episodes = episodes,
                        showLogos = iface.showLogos,
                        onUpdateBackdrop = previewViewModel::updateBackdrop,
                    )
                }
            }
        }
    }
}

@Composable
private fun DisplaySizeSettingsColumn(
    uiScale: Int,
    cardSize: Int,
    spacing: Int,
    onUiScaleChange: (Int) -> Unit,
    onCardSizeChange: (Int) -> Unit,
    onSpacingChange: (Int) -> Unit,
    onClickReset: () -> Unit,
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocus.tryRequestFocus() }

    Column(modifier = Modifier.fillMaxSize()) {
        TitleText(stringResource(R.string.display_size))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier =
                Modifier
                    .fillMaxHeight()
                    .focusRestorer(firstFocus),
        ) {
            item {
                DisplaySizeSlider(
                    preference = AppPreference.UiScale,
                    value = uiScale,
                    onChange = onUiScaleChange,
                    modifier = Modifier.focusRequester(firstFocus),
                )
            }
            item {
                DisplaySizeSlider(
                    preference = AppPreference.CardSize,
                    value = cardSize,
                    onChange = onCardSizeChange,
                )
            }
            item {
                DisplaySizeSlider(
                    preference = AppPreference.Spacing,
                    value = spacing,
                    onChange = onSpacingChange,
                )
            }
            item { HorizontalDivider() }
            item {
                HomeSettingsListItem(
                    selected = false,
                    headlineText = stringResource(R.string.reset_to_defaults),
                    leadingContent = {
                        Text(
                            text = stringResource(R.string.fa_arrows_rotate),
                            fontFamily = FontAwesome,
                        )
                    },
                    onClick = onClickReset,
                )
            }
        }
    }
}

@Composable
private fun DisplaySizeSlider(
    preference: AppSliderPreference<AppPreferences>,
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    SliderPreference(
        preference = preference,
        title = stringResource(preference.title),
        summary = preference.summary(context, value.toLong()),
        value = value.toLong(),
        onChange = { onChange(it.toInt()) },
        modifier = modifier,
    )
}

@Composable
private fun DisplaySizePreview(
    posters: List<BaseItem>,
    episodes: List<BaseItem>,
    showLogos: Boolean,
    onUpdateBackdrop: (BaseItem) -> Unit,
) {
    val recentlyAddedTitle = ResStringProvider(R.string.recently_added)
    val nextUpTitle = ResStringProvider(R.string.next_up)
    val previewRows =
        remember(posters, episodes) {
            listOf(
                HomeRowLoadingState.Success(title = recentlyAddedTitle, items = posters),
                HomeRowLoadingState.Success(title = nextUpTitle, items = episodes),
            )
        }
    var position by remember { mutableStateOf(RowColumn(0, 0)) }

    Row(modifier = Modifier.fillMaxSize()) {
        PreviewNavRail()
        HomePageContent(
            homeRows = previewRows,
            position = position,
            onFocusPosition = { position = it },
            onClickItem = { _, _ -> },
            onLongClickItem = { _, _ -> },
            onClickPlay = { _, _ -> },
            showClock = false,
            onUpdateBackdrop = onUpdateBackdrop,
            showLogo = showLogos,
            showViewMore = false,
            takeFocus = false,
            showEmptyRows = true,
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1f),
        )
    }
}

@Composable
private fun PreviewNavRail() {
    Column(
        modifier =
            Modifier
                .width(PREVIEW_NAV_RAIL_WIDTH_DP)
                .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PreviewNavRailIcons.forEach { icon ->
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(PREVIEW_NAV_ITEM_HEIGHT_DP),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(PREVIEW_NAV_ICON_SIZE_DP),
                )
            }
        }
    }
}
