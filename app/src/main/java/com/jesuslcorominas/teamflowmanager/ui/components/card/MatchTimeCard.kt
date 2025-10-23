package com.jesuslcorominas.teamflowmanager.ui.components.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.ui.components.form.AppTitle
import com.jesuslcorominas.teamflowmanager.ui.components.form.TitleSize
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.formatTime

@Composable
fun MatchTimeCard(
    match: Match,
    elapsedTimeMillis: Long? = null
) {
    val timeMillis = elapsedTimeMillis ?: match.elapsedTimeMillis
    val matchStatus = match.status
    val numberOfPeriods = match.numberOfPeriods
    val currentPeriod = match.currentPeriod
    val numberOfPauses = match.pauseCount

    // Calculate period duration
    val periodDurationMillis = if (numberOfPeriods == 2) {
        25 * 60 * 1000L // 25 minutes
    } else {
        (12 * 60 + 30) * 1000L // 12 minutes 30 seconds
    }

    // Calculate elapsed time in current period
    // Assume each previous period ran for its full duration
    // (This is an approximation since we don't track actual period durations)
    val elapsedInPreviousPeriods = (currentPeriod - 1) * periodDurationMillis
    val elapsedInCurrentPeriod = maxOf(0L, timeMillis - elapsedInPreviousPeriods)

    // Calculate remaining time (can be negative for stoppage time)
    val remainingTime = periodDurationMillis - elapsedInCurrentPeriod
    val isStoppageTime = remainingTime < 0
    val displayTime = if (isStoppageTime) -remainingTime else remainingTime

    // Determine period name
    val periodName = when {
        matchStatus == MatchStatus.SCHEDULED -> stringResource(R.string.match_next)
        matchStatus == MatchStatus.FINISHED -> stringResource(R.string.match_finished)
        matchStatus == MatchStatus.PAUSED && (numberOfPeriods == 2 || numberOfPauses == 2) -> stringResource(R.string.paused_match_half_time)
        matchStatus == MatchStatus.PAUSED && numberOfPeriods == 4 && (numberOfPauses == 1 || numberOfPauses == 3) -> stringResource(
            R.string.paused_match_quarter_break
        )

        numberOfPeriods == 2 && currentPeriod == 1 -> stringResource(R.string.first_half)
        numberOfPeriods == 2 && currentPeriod == 2 -> stringResource(R.string.second_half)
        numberOfPeriods == 4 && currentPeriod == 1 -> stringResource(R.string.first_quarter)
        numberOfPeriods == 4 && currentPeriod == 2 -> stringResource(R.string.second_quarter)
        numberOfPeriods == 4 && currentPeriod == 3 -> stringResource(R.string.third_quarter)
        numberOfPeriods == 4 && currentPeriod == 4 -> stringResource(R.string.fourth_quarter)
        else -> stringResource(R.string.period_label, currentPeriod, numberOfPeriods)
    }

    var expanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "chevronRotation"
    )

    AppCard(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TFMSpacing.spacing04, start = TFMSpacing.spacing04, end = TFMSpacing.spacing04),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedVisibility(visible = expanded) {
                    AppTitle(title = periodName, size = TitleSize.LARGE)
                }

                AnimatedVisibility(visible = expanded) {
                    AppTitle(
                        modifier = Modifier.padding(vertical = TFMSpacing.spacing02),
                        title = match.location,
                        size = TitleSize.MEDIUM,
                        startIcon = Icons.Default.LocationOn
                    )
                }

                // Scoreboard
                ScoreBoard(
                    teamName = match.teamName,
                    opponent = match.opponent,
                    goals = match.goals,
                    opponentGoals = match.opponentGoals,
                    expanded = expanded
                )

                Spacer(modifier = Modifier.padding(TFMSpacing.spacing01))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (isStoppageTime) {
                        AnimatedText(
                            text = formatTime(periodDurationMillis),
                            expanded = expanded
                        )
                        AnimatedText(
                            text = " + ",
                            color = MaterialTheme.colorScheme.error,
                            expanded = expanded
                        )
                        AnimatedText(
                            text = formatTime(displayTime),
                            color = MaterialTheme.colorScheme.error,
                            expanded = expanded
                        )
                    } else {
                        AnimatedText(
                            text = formatTime(displayTime),
                            expanded = expanded
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .padding(vertical = TFMSpacing.spacing02),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = stringResource(if (expanded) R.string.collapse else R.string.expand),
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun ScoreBoard(
    teamName: String,
    opponent: String,
    goals: Int,
    opponentGoals: Int,
    expanded: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TeamScore(
            modifier = Modifier.weight(1F),
            teamName = teamName,
            goalsCount = goals,
            expanded = expanded
        )

        Text(
            modifier = Modifier.padding(
                start = TFMSpacing.spacing06,
                end = TFMSpacing.spacing06,
                top = if (expanded) TFMSpacing.spacing05 else 0.dp,
            ),
            text = "-",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        TeamScore(
            modifier = Modifier.weight(1F),
            teamName = opponent,
            goalsCount = opponentGoals,
            isOpponent = true,
            expanded = expanded
        )
    }
}

@Composable
private fun TeamScore(
    modifier: Modifier = Modifier,
    teamName: String,
    goalsCount: Int,
    isOpponent: Boolean = false,
    expanded: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (isOpponent) Alignment.Start else Alignment.End,
    ) {
        AnimatedVisibility(visible = expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TFMSpacing.spacing05),
                contentAlignment = if (isOpponent) Alignment.CenterStart else Alignment.CenterEnd,
            ) {
                AppTitle(
                    title = teamName,
                    color = if (isOpponent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedText(
            text = "$goalsCount",
            start = MaterialTheme.typography.displaySmall,
            end = MaterialTheme.typography.displayLarge,
            color = if (isOpponent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            expanded = expanded
        )
    }
}

@Composable
fun AnimatedText(
    text: String,
    start: TextStyle = MaterialTheme.typography.bodyLarge,
    end: TextStyle = MaterialTheme.typography.displayMedium,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface,
    expanded: Boolean
) {
    val transition = updateTransition(targetState = expanded, label = "textStyleTransition")
    val fraction by transition.animateFloat(label = "fraction") { if (it) 1f else 0f }

    val animatedStyle = lerpTextStyle(start, end, fraction)

    Text(
        text = text,
        style = animatedStyle,
        fontWeight = fontWeight,
        color = color
    )

}

@Composable
fun lerpTextStyle(start: TextStyle, end: TextStyle, fraction: Float): TextStyle {
    return TextStyle(
        fontSize = lerp(start.fontSize, end.fontSize, fraction),
        lineHeight = lerp(start.lineHeight, end.lineHeight, fraction),
        letterSpacing = lerp(start.letterSpacing, end.letterSpacing, fraction),
        fontWeight = if (fraction < 0.5f) start.fontWeight else end.fontWeight,
        fontFamily = start.fontFamily ?: end.fontFamily
    )
}

@Preview(
    name = "Pixel 7 Pro",
    device = "spec:width=1440px,height=3120px,dpi=512",
    showSystemUi = true,
    showBackground = true
)
@Composable
fun MatchTimeCardPreview() {
    TFMAppTheme {
        Box(
            modifier = Modifier
                .padding(TFMSpacing.spacing04)
                .padding(top = 24.dp)
        ) {
            Match(
                id = 1,
                teamName = "Loyola D",
                opponent = "EFRO",
                location = "FUNDOMA",
                status = MatchStatus.IN_PROGRESS,
                elapsedTimeMillis = 15 * 60 * 1000L,
                numberOfPeriods = 2,
                currentPeriod = 1,
                pauseCount = 0,
                goals = 1,
                opponentGoals = 0,
            ).let { match ->
                MatchTimeCard(match = match)
            }
        }
    }
}
