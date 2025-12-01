package com.jesuslcorominas.teamflowmanager.ui.components.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.ui.components.form.AnimatedText
import com.jesuslcorominas.teamflowmanager.ui.components.form.AppTitle
import com.jesuslcorominas.teamflowmanager.ui.components.form.TitleSize
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.formatTime
import kotlinx.coroutines.launch

@Composable
fun MatchTimeCard(
    match: Match,
    currentTime: Long,
    onExport: (() -> Unit)? = null,
) {
    val periodName = getCurrentPeriodName(match)

    var expanded by remember { mutableStateOf(true) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "chevronRotation"
    )

    // region Lottie animation
    var lastGoals by remember { mutableIntStateOf(match.goals) }
    var showGoalAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(match.goals) {
        if (match.goals > lastGoals) {
            showGoalAnimation = true
        }
        lastGoals = match.goals
    }

    val confettiComposition by rememberLottieComposition(
        LottieCompositionSpec.Asset("animations/confetti.json")
    )

    val confettiAnimatable = rememberLottieAnimatable()
    val scope = rememberCoroutineScope()

    LaunchedEffect(expanded && showGoalAnimation) {
        confettiComposition?.let {
            scope
                .launch { launch { confettiAnimatable.animate(it, iterations = 1) } }
                .invokeOnCompletion { showGoalAnimation = false }
        }
    }
    // endregion

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = TFMSpacing.spacing04,
                            start = TFMSpacing.spacing04,
                            end = TFMSpacing.spacing04
                        ),
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

                    TimeBoard(
                        expanded = expanded,
                        currentTime = currentTime,
                        match = match
                    )

                    // Share button - only visible when match is finished and expanded
                    AnimatedVisibility(
                        visible = expanded && match.status == MatchStatus.FINISHED && onExport != null
                    ) {
                        IconButton(
                            onClick = { onExport?.invoke() },
                            modifier = Modifier.padding(top = TFMSpacing.spacing02)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.export_match_report_description),
                                tint = MaterialTheme.colorScheme.primary
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

        AnimatedVisibility(
            visible = showGoalAnimation,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LottieAnimation(
                composition = confettiComposition,
                progress = { confettiAnimatable.progress },
                modifier = Modifier.size(200.dp),
                renderMode = RenderMode.AUTOMATIC
            )
        }
    }
}

@Composable
private fun TimeBoard(match: Match, currentTime: Long, expanded: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        val periodName = getCurrentPeriodName(match)

        val currentPeriod =
            match.periods.firstOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }
                ?: match.periods.last()

        when (match.status) {
            MatchStatus.SCHEDULED, MatchStatus.PAUSED, MatchStatus.TIMEOUT -> AnimatedText(
                text = periodName,
                expanded = expanded
            )

            MatchStatus.IN_PROGRESS -> {
                val elapsedTime = currentTime - (currentPeriod.startTimeMillis)
                val displayTime = currentPeriod.periodDuration - elapsedTime
                val additionalTime = if (displayTime < 0L) -displayTime else 0L

                if (additionalTime <= 0) {
                    AnimatedText(
                        text = formatTime(displayTime),
                        expanded = expanded
                    )
                } else {
                    AnimatedText(
                        text = " + ",
                        color = MaterialTheme.colorScheme.error,
                        expanded = expanded
                    )
                    AnimatedText(
                        text = formatTime(additionalTime),
                        color = MaterialTheme.colorScheme.error,
                        expanded = expanded
                    )
                }
            }

            MatchStatus.FINISHED -> {
                if (expanded) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = TFMSpacing.spacing04),
                        verticalArrangement = Arrangement.spacedBy(space = TFMSpacing.spacing02)
                    ) {
                        match
                            .periods
                            .filter { it.startTimeMillis != 0L && it.endTimeMillis != 0L }
                            .forEach { period ->
                                val elapsedTime = period.endTimeMillis - period.startTimeMillis
                                val displayTime =
                                    if (elapsedTime < period.periodDuration) elapsedTime else period.periodDuration
                                val additionalTime =
                                    if (elapsedTime > period.periodDuration) elapsedTime - period.periodDuration else 0L

                                Row(horizontalArrangement = Arrangement.Start) {
                                    AnimatedText(
                                        text = formatTime(displayTime),
                                        end = MaterialTheme.typography.displaySmall,
                                        expanded = true
                                    )

                                    if (additionalTime > 0) {
                                        AnimatedText(
                                            text = " +",
                                            color = MaterialTheme.colorScheme.error,
                                            end = MaterialTheme.typography.displaySmall,
                                            expanded = true
                                        )
                                        AnimatedText(
                                            text = formatTime(additionalTime),
                                            color = MaterialTheme.colorScheme.error,
                                            end = MaterialTheme.typography.displaySmall,
                                            expanded = true
                                        )

                                    }
                                }
                            }
                    }
                } else {
                    AnimatedText(
                        text = periodName,
                        expanded = false
                    )
                }
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
private fun getCurrentPeriodName(match: Match): String {
    val matchStatus = match.status
    val numberOfPeriods = match.periodType.numberOfPeriods
    val numberOfPauses = match.pauseCount

    val currentPeriod = match
        .periods
        .firstOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }
        ?: match.periods.last()

    return when {
        matchStatus == MatchStatus.SCHEDULED -> stringResource(R.string.match_next)
        matchStatus == MatchStatus.FINISHED -> stringResource(R.string.match_finished)
        matchStatus == MatchStatus.TIMEOUT -> stringResource(R.string.match_timeout)
        matchStatus == MatchStatus.PAUSED
            && (match.periodType == PeriodType.HALF_TIME || numberOfPauses == 2) ->
            stringResource(R.string.paused_match_half_time)

        matchStatus == MatchStatus.PAUSED
            && match.periodType == PeriodType.QUARTER_TIME
            && (numberOfPauses == 1 || numberOfPauses == 3) ->
            stringResource(R.string.paused_match_quarter_break)

        match.periodType == PeriodType.HALF_TIME
            && currentPeriod.periodNumber == 1 -> stringResource(R.string.first_half)

        match.periodType == PeriodType.HALF_TIME && currentPeriod.periodNumber == 2 ->
            stringResource(R.string.second_half)

        match.periodType == PeriodType.QUARTER_TIME && currentPeriod.periodNumber == 1 ->
            stringResource(R.string.first_quarter)

        match.periodType == PeriodType.QUARTER_TIME && currentPeriod.periodNumber == 2 ->
            stringResource(R.string.second_quarter)

        match.periodType == PeriodType.QUARTER_TIME && currentPeriod.periodNumber == 3 ->
            stringResource(R.string.third_quarter)

        match.periodType == PeriodType.QUARTER_TIME && currentPeriod.periodNumber == 4 ->
            stringResource(R.string.fourth_quarter)

        else ->
            stringResource(R.string.period_label, currentPeriod.periodNumber, numberOfPeriods)
    }
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
            val currentTime = System.currentTimeMillis()

            Match(
                id = 1,
                teamName = "Loyola D",
                opponent = "EFRO",
                location = "FUNDOMA",
                status = MatchStatus.IN_PROGRESS,
                periodType = PeriodType.HALF_TIME,
                periods = listOf(
                    MatchPeriod(
                        periodNumber = 1,
                        periodDuration = 25 * 60 * 1000L,
                        startTimeMillis = 0L,
                        endTimeMillis = 0L,
                    ), MatchPeriod(
                        periodNumber = 2,
                        periodDuration = 25 * 60 * 1000L,
                        startTimeMillis = 0L,
                        endTimeMillis = 0L,
                    )
                ),
                pauseCount = 0,
                goals = 1,
                captainId = 2L,
                opponentGoals = 0,
            ).let { match ->
                MatchTimeCard(match = match, currentTime)
            }
        }
    }
}
