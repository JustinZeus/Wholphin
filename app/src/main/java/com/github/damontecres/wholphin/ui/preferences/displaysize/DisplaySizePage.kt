package com.github.damontecres.wholphin.ui.preferences.displaysize

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.contentColorFor
import com.github.damontecres.wholphin.R
import com.github.damontecres.wholphin.data.model.BaseItem
import com.github.damontecres.wholphin.preferences.AppChoicePreference
import com.github.damontecres.wholphin.preferences.AppPreference
import com.github.damontecres.wholphin.preferences.AppPreferences
import com.github.damontecres.wholphin.preferences.DisplaySizeLevel
import com.github.damontecres.wholphin.preferences.updateInterfacePreferences
import com.github.damontecres.wholphin.services.BackdropService
import com.github.damontecres.wholphin.ui.SlimItemFields
import com.github.damontecres.wholphin.ui.components.TimeDisplay
import com.github.damontecres.wholphin.ui.data.RowColumn
import com.github.damontecres.wholphin.ui.handleDPadKeyEvents
import com.github.damontecres.wholphin.ui.launchIO
import com.github.damontecres.wholphin.ui.main.HomePageContent
import com.github.damontecres.wholphin.ui.main.settings.TitleText
import com.github.damontecres.wholphin.ui.preferences.PreferenceSummary
import com.github.damontecres.wholphin.ui.preferences.PreferenceTitle
import com.github.damontecres.wholphin.ui.preferences.PreferencesViewModel
import com.github.damontecres.wholphin.ui.theme.AppTypography
import com.github.damontecres.wholphin.ui.theme.toMaterialTypography
import com.github.damontecres.wholphin.ui.tryRequestFocus
import com.github.damontecres.wholphin.ui.util.ResStringProvider
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
private val DisplaySizeSettingsWidth = 300.dp

private val PreviewNavRailIcons: List<ImageVector> =
    listOf(
        Icons.Default.Person,
        Icons.Default.Home,
        Icons.Default.Favorite,
        Icons.Default.Search,
        Icons.Default.Add,
        Icons.Default.Star,
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
                        fields = SlimItemFields,
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

    val posters by previewViewModel.posters.collectAsState()
    val episodes by previewViewModel.episodes.collectAsState()

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1f),
        ) {
            DisplaySizePreview(
                posters = posters,
                episodes = episodes,
                showLogos = iface.showLogos,
                onUpdateBackdrop = previewViewModel::updateBackdrop,
            )
            if (iface.showClock) {
                TimeDisplay()
            }
        }

        Box(
            modifier =
                Modifier
                    .width(DisplaySizeSettingsWidth)
                    .fillMaxHeight()
                    .background(color = MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
        ) {
            // Lock the settings-panel chrome to base typography so its labels
            // don't reflow under the user as they cycle Text size levels.
            androidx.compose.material3.MaterialTheme(typography = AppTypography.toMaterialTypography()) {
                MaterialTheme(typography = AppTypography) {
                    DisplaySizeSettingsColumn(
                        textLevel = iface.textSizeLevel,
                        cardLevel = iface.cardSizeLevel,
                        spacingLevel = iface.spacingLevel,
                        onChangeTextLevel = { value ->
                            scope.launch {
                                viewModel.preferenceDataStore.updateData {
                                    it.updateInterfacePreferences { textSizeLevel = value }
                                }
                            }
                        },
                        onChangeCardLevel = { value ->
                            scope.launch {
                                viewModel.preferenceDataStore.updateData {
                                    it.updateInterfacePreferences { cardSizeLevel = value }
                                }
                            }
                        },
                        onChangeSpacingLevel = { value ->
                            scope.launch {
                                viewModel.preferenceDataStore.updateData {
                                    it.updateInterfacePreferences { spacingLevel = value }
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DisplaySizeSettingsColumn(
    textLevel: DisplaySizeLevel,
    cardLevel: DisplaySizeLevel,
    spacingLevel: DisplaySizeLevel,
    onChangeTextLevel: (DisplaySizeLevel) -> Unit,
    onChangeCardLevel: (DisplaySizeLevel) -> Unit,
    onChangeSpacingLevel: (DisplaySizeLevel) -> Unit,
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocus.tryRequestFocus() }

    Column(modifier = Modifier.fillMaxSize()) {
        TitleText(stringResource(R.string.display_size))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier =
                Modifier
                    .fillMaxHeight()
                    .focusRestorer(firstFocus),
        ) {
            item {
                LevelTickSlider(
                    preference = AppPreference.TextSize,
                    currentValue = textLevel,
                    onChange = onChangeTextLevel,
                    modifier = Modifier.focusRequester(firstFocus),
                )
            }
            item {
                LevelTickSlider(
                    preference = AppPreference.CardSize,
                    currentValue = cardLevel,
                    onChange = onChangeCardLevel,
                )
            }
            item {
                LevelTickSlider(
                    preference = AppPreference.Spacing,
                    currentValue = spacingLevel,
                    onChange = onChangeSpacingLevel,
                )
            }
        }
    }
}

@Composable
private fun LevelTickSlider(
    preference: AppChoicePreference<AppPreferences, DisplaySizeLevel>,
    currentValue: DisplaySizeLevel,
    onChange: (DisplaySizeLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = stringArrayResource(preference.displayValues).toList()
    val tickCount = labels.size
    val currentIndex = preference.valueToIndex(currentValue).coerceIn(0, tickCount - 1)
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val background =
        if (focused) MaterialTheme.colorScheme.inverseSurface else Color.Unspecified
    val contentColor = contentColorFor(background)
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .background(background, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .handleDPadKeyEvents(
                    onLeft = {
                        if (currentIndex > 0) {
                            onChange(preference.indexToValue(currentIndex - 1))
                        }
                    },
                    onRight = {
                        if (currentIndex < tickCount - 1) {
                            onChange(preference.indexToValue(currentIndex + 1))
                        }
                    },
                ).focusable(interactionSource = interactionSource),
    ) {
        PreferenceTitle(stringResource(preference.title), color = contentColor)
        ScrollbarTrack(
            currentIndex = currentIndex,
            tickCount = tickCount,
            activeColor = activeColor,
            inactiveColor = inactiveColor,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(24.dp),
        )
        PreferenceSummary(labels.getOrNull(currentIndex), color = contentColor)
    }
}

@Composable
private fun ScrollbarTrack(
    currentIndex: Int,
    tickCount: Int,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val trackRadius = size.height / 2f
        drawRoundRect(
            color = inactiveColor,
            cornerRadius = CornerRadius(trackRadius, trackRadius),
        )
        if (tickCount <= 0) return@Canvas
        val padding = size.height * 0.06f
        val thumbHeight = size.height - padding * 2f
        val thumbRadius = thumbHeight / 2f
        val slotWidth = (size.width - padding * 2f) / tickCount
        val thumbWidthIdeal = slotWidth * 0.9f
        val thumbWidth = thumbWidthIdeal.coerceAtLeast(thumbHeight)
        val slotCenter = padding + slotWidth * (currentIndex + 0.5f)
        val thumbLeft = (slotCenter - thumbWidth / 2f).coerceIn(padding, size.width - padding - thumbWidth)
        drawRoundRect(
            color = activeColor,
            topLeft = Offset(thumbLeft, padding),
            size = Size(thumbWidth, thumbHeight),
            cornerRadius = CornerRadius(thumbRadius, thumbRadius),
        )
    }
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
                HomeRowLoadingState.Success(title = nextUpTitle, items = episodes),
                HomeRowLoadingState.Success(title = recentlyAddedTitle, items = posters),
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
