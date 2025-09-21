package com.isaakhanimann.journal.ui.tabs.journal.experience.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.CardWithTitle
import com.isaakhanimann.journal.ui.tabs.search.substance.BulletPoints
import com.isaakhanimann.journal.ui.tabs.search.substance.SectionText

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ExplainTimelineScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.explain_timeline_title)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardWithTitle(title = stringResource(R.string.explain_timeline_assumptions_title)) {
                val text = buildAnnotatedString {
                    append(stringResource(R.string.explain_timeline_assumptions_intro_part1))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                        append(stringResource(R.string.explain_timeline_assumptions_intro_part2_bold))
                    }
                    append(stringResource(R.string.explain_timeline_assumptions_intro_part3))
                }
                SectionText(text = text)
                BulletPoints(
                    points = stringArrayResource(R.array.explain_timeline_assumptions_bullets).toList()
                )
            }
            CardWithTitle(title = stringResource(R.string.explain_timeline_understanding_title)) {
                BulletPoints(
                    points = stringArrayResource(R.array.explain_timeline_understanding_bullets).toList()
                )
            }
            CardWithTitle(title = stringResource(R.string.explain_timeline_pw_durations_title)) {
                SectionText(
                    text = stringResource(R.string.explain_timeline_pw_durations_intro)
                )
                val titleStyle = MaterialTheme.typography.titleSmall
                Text(text = stringResource(R.string.explain_timeline_pw_durations_total_title), style = titleStyle)
                SectionText(text = stringResource(R.string.explain_timeline_pw_durations_total_desc))
                Text(text = stringResource(R.string.explain_timeline_pw_durations_onset_title), style = titleStyle)
                SectionText(text = stringResource(R.string.explain_timeline_pw_durations_onset_desc))
                Text(text = stringResource(R.string.explain_timeline_pw_durations_comeup_title), style = titleStyle)
                SectionText(text = stringResource(R.string.explain_timeline_pw_durations_comeup_desc))
                Text(text = stringResource(R.string.explain_timeline_pw_durations_peak_title), style = titleStyle)
                SectionText(text = stringResource(R.string.explain_timeline_pw_durations_peak_desc))
                Text(text = stringResource(R.string.explain_timeline_pw_durations_offset_title), style = titleStyle)
                SectionText(text = stringResource(R.string.explain_timeline_pw_durations_offset_desc))
                Text(text = stringResource(R.string.explain_timeline_pw_durations_after_effects_title), style = titleStyle)
                SectionText(
                    text = stringResource(R.string.explain_timeline_pw_durations_after_effects_desc)
                )
            }
        }
    }
}