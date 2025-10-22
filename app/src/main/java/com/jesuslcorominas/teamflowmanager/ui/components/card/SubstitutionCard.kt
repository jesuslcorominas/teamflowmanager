package com.jesuslcorominas.teamflowmanager.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.players.components.JerseyBadge
import com.jesuslcorominas.teamflowmanager.ui.theme.SubstitutionGreen
import com.jesuslcorominas.teamflowmanager.ui.theme.SubstitutionRed
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.formatTime
import com.jesuslcorominas.teamflowmanager.viewmodel.SubstitutionItem

@Composable
fun SubstitutionCard(substitution: SubstitutionItem) {
    AppCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlayerSubstitution(
                    modifier = Modifier.weight(1f),
                    name = "${substitution.playerIn.firstName} ${substitution.playerIn.lastName}",
                    number = substitution.playerIn.number,
                    playerIn = true
                )

                Text(
                    modifier = Modifier.padding(bottom = 24.dp),
                    text = formatTime(substitution.matchElapsedTimeMillis),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                PlayerSubstitution(
                    modifier = Modifier.weight(1f),
                    name = "${substitution.playerOut.firstName} ${substitution.playerOut.lastName}",
                    number = substitution.playerOut.number,
                    playerIn = false
                )
            }
        }
    }
}

@Composable
private fun PlayerSubstitution(modifier: Modifier = Modifier, name: String, number: Int, playerIn: Boolean) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        JerseyBadge(
            number = number,
            cornerShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
        )

        val slantedSidesShape: Shape = GenericShape { size: Size, _ ->
            val offset = size.height / 2

            moveTo(offset, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - offset, size.height)
            lineTo(0f, size.height)
            close()
        }

        Box(
            modifier = Modifier
                .padding(bottom = 6.dp)
                .width(96.dp)
                .height(8.dp)
                .clip(slantedSidesShape)
                .background(if (playerIn) SubstitutionGreen else SubstitutionRed)
        )

        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun SubstitutionCardPreview() {
    val substitution = SubstitutionItem(
        matchElapsedTimeMillis = 150000L,
        playerOut = Player(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            number = 10,
            positions = listOf(Position.Forward),
            teamId = 1,
        ),
        playerIn = Player(
            id = 2,
            firstName = "Jane",
            lastName = "Smith",
            number = 5,
            positions = listOf(Position.Defender),
            teamId = 1,
        ),
    )

    MaterialTheme {
        SubstitutionCard(substitution)
    }
}
