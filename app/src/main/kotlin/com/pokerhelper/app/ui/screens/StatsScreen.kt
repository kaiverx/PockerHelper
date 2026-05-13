package com.pokerhelper.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokerhelper.app.R
import com.pokerhelper.app.ui.theme.PokerHelperTheme
import com.pokerhelper.app.viewmodel.StatsViewModel
import com.pokerhelper.core.model.HandOutcome
import com.pokerhelper.core.model.PlayedHand
import java.text.SimpleDateFormat

import java.util.Date
import java.util.Locale

private enum class StatsFilter { ALL, WON, FOLDED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel, onBack: () -> Unit) {
    val stats by viewModel.stats.collectAsState()
    val allHands by viewModel.hands.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(StatsFilter.ALL) }

    val filteredHands = when (filter) {
        StatsFilter.ALL -> allHands
        StatsFilter.WON -> allHands.filter { it.outcome == HandOutcome.WON }
        StatsFilter.FOLDED -> allHands.filter { it.outcome == HandOutcome.FOLDED }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    if (stats.total > 0) {
                        TextButton(onClick = { showClearDialog = true }) {
                            Text(stringResource(R.string.action_clear))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Сводка сверху
            SummaryCard(stats)

            // Фильтр
            FilterTabs(
                selected = filter,
                counts = mapOf(
                    StatsFilter.ALL to stats.total,
                    StatsFilter.WON to stats.won,
                    StatsFilter.FOLDED to stats.folded
                ),
                onSelect = { filter = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(10.dp))

            // Список
            if (filteredHands.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        when {
                            stats.total == 0 -> stringResource(R.string.stats_empty_total)
                            filter == StatsFilter.WON -> stringResource(R.string.stats_empty_won)
                            filter == StatsFilter.FOLDED -> stringResource(R.string.stats_empty_folded)
                            else -> "—"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    items(filteredHands) { hand ->
                        HandRow(hand)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.stats_clear_title)) },
            text = { Text(stringResource(R.string.stats_clear_text, stats.total)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearDialog = false
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun SummaryCard(stats: com.pokerhelper.app.data.HandStats) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatCell(
                label = stringResource(R.string.stats_winrate),
                value = stats.winRatePercent(),
                sub = stringResource(R.string.stats_won_of_total, stats.won, stats.total)
            )
            VerticalDivider()
            StatCell(
                label = stringResource(R.string.stats_avg_win_equity),
                value = stats.averageWinEquityPercent(),
                sub = if (stats.won > 0) stringResource(R.string.stats_by_n_hands, stats.won)
                      else stringResource(R.string.stats_no_data)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterTabs(
    selected: StatsFilter,
    counts: Map<StatsFilter, Int>,
    onSelect: (StatsFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val labels = mapOf(
        StatsFilter.ALL to stringResource(R.string.stats_filter_all),
        StatsFilter.WON to stringResource(R.string.stats_filter_won),
        StatsFilter.FOLDED to stringResource(R.string.stats_filter_folded)
    )
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        StatsFilter.values().forEachIndexed { idx, f ->
            SegmentedButton(
                selected = selected == f,
                onClick = { onSelect(f) },
                shape = SegmentedButtonDefaults.itemShape(idx, StatsFilter.values().size),
                label = {
                    Text(stringResource(R.string.stats_filter_count_format, labels[f] ?: "", counts[f] ?: 0))
                }
            )
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(
            value,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            sub,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(60.dp)
            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
    )
}

@Composable
private fun HandRow(hand: PlayedHand) {
    val semantic = PokerHelperTheme.semantic
    val dateFormat = remember { SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()) }
    val isWin = hand.outcome == HandOutcome.WON

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWin) MaterialTheme.colorScheme.surface
                             else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isWin) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StartingHand(hand.holeCards)
                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val category = hand.finalHandCategory
                    if (isWin && category != null) {
                        Text(
                            category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            stringResource(R.string.stats_hand_fold_label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = semantic.fold
                        )
                    }

                    hand.equityPercent?.let { eq ->
                        val formatted = "%.0f%%".format(eq)
                        Text(
                            if (isWin)
                                stringResource(R.string.stats_hand_won_equity_format, formatted)
                            else
                                stringResource(R.string.stats_hand_fold_equity_format, formatted),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isWin) semantic.win else semantic.fold
                        )
                    }

                    Text(
                        stringResource(
                            R.string.stats_hand_vs_format,
                            hand.opponents,
                            dateFormat.format(Date(hand.timestamp))
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutcomeBadge(isWin)
            }

            if (hand.boardCards.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.stats_hand_board_format, hand.boardCards.joinToString(" ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OutcomeBadge(isWin: Boolean) {
    val semantic = PokerHelperTheme.semantic
    val bg = if (isWin) semantic.win else semantic.fold
    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isWin) Icons.Default.Check else Icons.Default.Close,
            contentDescription = stringResource(if (isWin) R.string.cd_won else R.string.cd_fold),
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            stringResource(if (isWin) R.string.stats_badge_won else R.string.stats_badge_fold),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun StartingHand(cards: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        cards.forEach { c -> MiniCard(c) }
    }
}

@Composable
private fun MiniCard(shortCard: String) {
    val semantic = PokerHelperTheme.semantic
    val rank = shortCard.dropLast(1)
    val suit = shortCard.last()
    val (suitSymbol, isRed) = when (suit) {
        'c' -> "♣" to false
        'd' -> "♦" to true
        'h' -> "♥" to true
        's' -> "♠" to false
        else -> "?" to false
    }
    val color = if (isRed) semantic.cardRed else semantic.cardBlack

    Box(
        modifier = Modifier
            .size(width = 38.dp, height = 52.dp)
            .background(Color.White, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(rank, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(suitSymbol, color = color, fontSize = 13.sp)
        }
    }
}
