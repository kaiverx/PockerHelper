package com.pokerhelper.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokerhelper.app.ui.theme.PokerHelperTheme
import com.pokerhelper.core.model.Card
import com.pokerhelper.core.model.Rank
import com.pokerhelper.core.model.Suit


@Composable
fun CardPicker(
    selectedCards: Set<Card>,
    outsByCard: Map<Card, Double>,
    onCardClick: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Suit.values().forEach { suit ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Rank.values().reversed().forEach { rank ->
                    val card = Card(rank, suit)
                    CardCell(
                        card = card,
                        selected = card in selectedCards,
                        outWinRate = outsByCard[card],
                        onClick = { onCardClick(card) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardCell(
    card: Card,
    selected: Boolean,
    outWinRate: Double?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = PokerHelperTheme.semantic
    val isRed = card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS
    val isOut = outWinRate != null

    val textColor = if (isRed) semantic.cardRed else semantic.cardBlack

    val bgColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        isOut -> semantic.outAccent
        else -> Color.White
    }
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.primary
        isOut -> semantic.outBorder
        else -> Color(0xFFCCCCCC)
    }
    val borderWidth = if (selected || isOut) 2.dp else 1.dp

    Box(
        modifier = modifier
            .aspectRatio(0.7f)
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .alpha(if (selected) 0.55f else 1f),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = card.rank.symbol,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = card.suit.symbol,
                color = textColor,
                fontSize = 12.sp
            )
            if (outWinRate != null) {
                Text(
                    text = "${(outWinRate * 100).toInt()}%",
                    color = Color(0xFF3E2723), // тёмно-коричневый — контрастно к оранжевому
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
