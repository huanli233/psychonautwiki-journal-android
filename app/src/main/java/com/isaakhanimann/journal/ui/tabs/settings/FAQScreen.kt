package com.isaakhanimann.journal.ui.tabs.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.ui.tabs.search.substance.SectionWithTitle

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun FAQScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.faq_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuestionAnswerRow(
                questionResId = R.string.faq_q1_info_source,
                answerResId = R.string.faq_a1_info_source
            )
            QuestionAnswerRow(
                questionResId = R.string.faq_q2_interactions,
                answerResId = R.string.faq_a2_interactions
            )
            QuestionAnswerRow(
                questionResId = R.string.faq_q3_changing_info,
                answerResId = R.string.faq_a3_changing_info
            )
            QuestionAnswerRow(
                questionResId = R.string.faq_q4_timeline_independence,
                answerResId = R.string.faq_a4_timeline_independence
            )
            QuestionAnswerRow(
                questionResId = R.string.faq_q5_dose_dots,
                answerResId = R.string.faq_a5_dose_dots
            )
        }
    }
}

@Composable
fun QuestionAnswerRow(questionResId: Int, answerResId: Int) {
    SectionWithTitle(title = stringResource(id = questionResId)) {
        Text(
            text = stringResource(id = answerResId),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}