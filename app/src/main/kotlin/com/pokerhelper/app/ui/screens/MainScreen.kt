package com.pokerhelper.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pokerhelper.app.R
import com.pokerhelper.app.ui.components.CardPicker
import com.pokerhelper.app.ui.theme.PokerHelperTheme
import com.pokerhelper.app.viewmodel.CardSlot
import com.pokerhelper.app.viewmodel.HandAnalysisState
import com.pokerhelper.app.viewmodel.MainViewModel
import com.pokerhelper.core.model.Card
import com.pokerhelper.core.model.HandOutcome
import com.pokerhelper.core.model.Suit
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    // Резолвим enum-сообщение в локализованную строку перед показом
    val wonMsg = stringResource(R.string.analyzer_saved_won)
    val foldMsg = stringResource(R.string.analyzer_saved_fold)
    val needHolesMsg = stringResource(R.string.analyzer_need_holes_to_save)
    LaunchedEffect(state.savedMessage) {
        state.savedMessage?.let { msg ->
            val text = when (msg) {
                com.pokerhelper.app.viewmodel.SavedMessage.WON_RECORDED -> wonMsg
                com.pokerhelper.app.viewmodel.SavedMessage.FOLD_RECORDED -> foldMsg
                com.pokerhelper.app.viewmodel.SavedMessage.NEED_HOLE_CARDS -> needHolesMsg
            }
            snackbarHost.showSnackbar(text)
            delay(50)
            viewModel.acknowledgeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.analyzer_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::clear) {
                        Text(stringResource(R.string.action_reset))
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
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            HoleCardsRow(state, viewModel::selectSlot)
            Spacer(Modifier.height(14.dp))
            BoardCardsRow(state, viewModel::selectSlot)

            Spacer(Modifier.height(8.dp))
            HintText(state)

            Spacer(Modifier.height(12.dp))
            OpponentSelector(state.opponents, viewModel::setOpponents)

            Spacer(Modifier.height(12.dp))
            ResultPanel(state)

            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(R.string.analyzer_pick_card),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))

            val outsByCard = state.outsResult?.outs
                ?.associate { it.card to it.winRate }
                ?: emptyMap()
            CardPicker(
                selectedCards = state.allKnownCards,
                outsByCard = outsByCard,
                onCardClick = viewModel::pickCard
            )

            Spacer(Modifier.height(18.dp))
            OutcomeButtons(
                enabled = state.holeCardsList.size == 2,
                onFold = { viewModel.recordOutcome(HandOutcome.FOLDED) },
                onWon = { viewModel.recordOutcome(HandOutcome.WON) }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OutcomeButtons(enabled: Boolean, onFold: () -> Unit, onWon: () -> Unit) {
    val semantic = PokerHelperTheme.semantic
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onFold,
            enabled = enabled,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text(
                stringResource(R.string.analyzer_btn_fold),
                fontWeight = FontWeight.Bold
            )
        }
        Button(
            onClick = onWon,
            enabled = enabled,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = semantic.win)
        ) {
            Text(
                stringResource(R.string.analyzer_btn_won),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun HoleCardsRow(state: HandAnalysisState, onSlotClick: (CardSlot) -> Unit) {
    Column {
        SectionLabel(stringResource(R.string.analyzer_hole_cards))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(2) { i ->
                CardSlotView(
                    card = state.holeCards[i],
                    isSelected = state.selectedSlot == CardSlot.Hole(i),
                    onClick = { onSlotClick(CardSlot.Hole(i)) }
                )
            }
        }
    }
}

@Composable
private fun BoardCardsRow(state: HandAnalysisState, onSlotClick: (CardSlot) -> Unit) {
    Column {
        SectionLabel(stringResource(R.string.analyzer_board))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(5) { i ->
                CardSlotView(
                    card = state.boardCards[i],
                    isSelected = state.selectedSlot == CardSlot.Board(i),
                    onClick = { onSlotClick(CardSlot.Board(i)) }
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun CardSlotView(card: Card?, isSelected: Boolean, onClick: () -> Unit) {
    val semantic = PokerHelperTheme.semantic
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        card != null -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }
    val borderWidth = if (isSelected) 3.dp else 1.dp

    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 62.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (card != null) {
            val red = card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS
            Text(
                card.toString(),
                color = if (red) semantic.cardRed else semantic.cardBlack,
                fontWeight = FontWeight.Bold
            )
        } else if (isSelected) {
            Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HintText(state: HandAnalysisState) {
    val text = when {
        state.selectedSlot != null -> stringResource(R.string.analyzer_hint_slot_selected)
        state.allKnownCards.isEmpty() -> stringResource(R.string.analyzer_hint_empty)
        else -> stringResource(R.string.analyzer_hint_default)
    }
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun OpponentSelector(current: Int, onChange: (Int) -> Unit) {
    Column {
        Text(
            stringResource(R.string.analyzer_opponents, current),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Slider(
            value = current.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 1f..9f,
            steps = 7
        )
    }
}

@Composable
private fun ResultPanel(state: HandAnalysisState) {
    val semantic = PokerHelperTheme.semantic
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when {
                state.isCalculating -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.analyzer_calculating))
                    }
                }
                state.error != null -> {
                    Text(
                        stringResource(R.string.analyzer_error_prefix, state.error!!),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                state.holeCardsList.size < 2 -> {
                    Text(stringResource(R.string.analyzer_need_hole_cards))
                }
                else -> {
                    state.handRank?.let {
                        Text(
                            stringResource(R.string.analyzer_current_hand),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            it.category.displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    state.equity?.let {
                        Text(
                            stringResource(R.string.analyzer_equity_format, state.opponents, it.equityPercent()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            stringResource(R.string.analyzer_win_format, it.winPercent(), it.iterations),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    state.outsResult?.let { outsResult ->
                        if (outsResult.outs.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.analyzer_outs_header, outsResult.outs.size),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                stringResource(R.string.analyzer_any_out_format, outsResult.anyOutPercent()),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = semantic.outBorder
                            )
                            Spacer(Modifier.height(4.dp))
                            val topOuts = outsResult.outs.take(8).joinToString(", ") {
                                "${it.card} ${it.winPercent()}/${it.probabilityPercent()}"
                            }
                            Text(
                                topOuts,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                stringResource(R.string.analyzer_outs_format_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
