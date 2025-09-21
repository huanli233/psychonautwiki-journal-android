package com.isaakhanimann.journal.ui.tabs.journal.experience.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import com.isaakhanimann.journal.ui.utils.getShortTimeText
import com.isaakhanimann.journal.ui.utils.getShortTimeWithWeekdayText
import com.isaakhanimann.journal.ui.utils.getShortWeekdayText
import java.time.Instant

const val TIME_RANGE_SEPARATOR_TEXT = " - "

@Composable
fun IngestionTimeOrDurationText(
    time: Instant,
    endTime: Instant?,
    index: Int,
    timeDisplayOption: TimeDisplayOption,
    allTimesSortedMap: List<Instant>
) {
    val textStyle = MaterialTheme.typography.bodyMedium
    val secondaryTextStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    when (timeDisplayOption) {
        TimeDisplayOption.REGULAR -> {
            RegularTimeOrRangeText(time, endTime, secondaryTextStyle)
        }

        TimeDisplayOption.RELATIVE_TO_NOW -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TimeRelativeToNowText(
                    time = time,
                    style = secondaryTextStyle
                )
                if (endTime != null) {
                    Text(TIME_RANGE_SEPARATOR_TEXT, style = secondaryTextStyle)
                    TimeRelativeToNowText(
                        time = endTime,
                        style = secondaryTextStyle
                    )
                }
            }
        }

        TimeDisplayOption.TIME_BETWEEN -> {
            if (index == 0) {
                RegularTimeOrRangeText(time, endTime, secondaryTextStyle)
            } else {
                val previousTime =
                    allTimesSortedMap[index - 1]
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (endTime != null) {
                        Text(
                            text = getDurationText(
                                fromInstant = previousTime,
                                toInstant = time
                            ),
                            style = secondaryTextStyle
                        )
                        Text(TIME_RANGE_SEPARATOR_TEXT, style = secondaryTextStyle)
                        Text(
                            text = getDurationText(
                                fromInstant = previousTime,
                                toInstant = endTime
                            ) + " after previous",
                            style = secondaryTextStyle
                        )
                    } else {
                        Text(
                            text = getDurationText(
                                fromInstant = previousTime,
                                toInstant = time
                            ) + " after previous",
                            style = secondaryTextStyle
                        )
                    }
                }
            }
        }

        TimeDisplayOption.RELATIVE_TO_START -> {
            if (index == 0) {
                RegularTimeOrRangeText(time, endTime, secondaryTextStyle)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val startTime = allTimesSortedMap.firstOrNull()
                        ?: Instant.now()
                    if (endTime != null) {
                        Text(
                            text = getDurationText(
                                fromInstant = startTime,
                                toInstant = time
                            ),
                            style = secondaryTextStyle
                        )
                        Text(TIME_RANGE_SEPARATOR_TEXT, style = secondaryTextStyle)
                        Text(
                            text = getDurationText(
                                fromInstant = startTime,
                                toInstant = endTime
                            ) + " after start",
                            style = secondaryTextStyle
                        )
                    } else {
                        Text(
                            text = getDurationText(
                                fromInstant = startTime,
                                toInstant = time
                            ) + " after start",
                            style = secondaryTextStyle
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegularTimeOrRangeText(
    time: Instant,
    endTime: Instant?,
    textStyle: TextStyle
) {
    val startText = time.getShortTimeWithWeekdayText()
    val text = if (endTime == null) {
        startText
    } else {
        val startDay = time.getShortWeekdayText()
        val endDay = endTime.getShortWeekdayText()
        if (startDay == endDay) {
            startText + TIME_RANGE_SEPARATOR_TEXT + endTime.getShortTimeText()
        } else {
            startText + TIME_RANGE_SEPARATOR_TEXT + endTime.getShortTimeWithWeekdayText()
        }
    }
    Text(
        text = text,
        style = textStyle
    )
}

@Composable
fun NoteOrRatingTimeOrDurationText(
    time: Instant,
    timeDisplayOption: TimeDisplayOption,
    firstIngestionTime: Instant
) {
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    when (timeDisplayOption) {
        TimeDisplayOption.REGULAR, TimeDisplayOption.TIME_BETWEEN -> {
            val startText = time.getShortTimeWithWeekdayText()
            Text(text = startText, style = textStyle)
        }

        TimeDisplayOption.RELATIVE_TO_NOW -> {
            TimeRelativeToNowText(
                time = time,
                style = textStyle
            )
        }

        TimeDisplayOption.RELATIVE_TO_START -> {
            val durationText = getDurationText(
                fromInstant = firstIngestionTime,
                toInstant = time
            ) + if (firstIngestionTime < time) " after start" else " before start"
            Text(
                text = durationText,
                style = textStyle
            )
        }
    }
}