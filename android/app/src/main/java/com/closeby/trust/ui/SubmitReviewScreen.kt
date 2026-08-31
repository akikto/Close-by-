package com.closeby.trust.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.closeby.trust.domain.model.ReviewerRole
import com.closeby.trust.presentation.SubmitReviewFormState

@Composable
fun SubmitReviewScreen(
    form: SubmitReviewFormState,
    role: ReviewerRole,
    onOverallRatingChange: (Int) -> Unit,
    onServiceQualityChange: (Int) -> Unit,
    onBehaviourChange: (Int) -> Unit,
    onReliabilityChange: (Int) -> Unit,
    onProfessionalismChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(form.serviceTitle, style = MaterialTheme.typography.titleLarge)
        Text("Role: ${role.name.lowercase().replaceFirstChar { it.titlecase() }}")

        if (form.alreadyReviewed) {
            Text("You already submitted a review for this request.")
            return@Column
        }

        RatingRow("Overall rating", form.overallRating, onOverallRatingChange)
        if (role == ReviewerRole.CUSTOMER) {
            RatingRow("Service quality", form.serviceQuality, onServiceQualityChange)
        } else {
            RatingRow("Professionalism", form.professionalism, onProfessionalismChange)
        }
        RatingRow("Behaviour", form.behaviour, onBehaviourChange)
        RatingRow("Reliability", form.reliability, onReliabilityChange)

        OutlinedTextField(
            value = form.comment,
            onValueChange = onCommentChange,
            label = { Text("Comment (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            enabled = form.overallRating in 1..5
        ) {
            Text("Submit review")
        }
    }
}

@Composable
private fun RatingRow(
    label: String,
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { value ->
                val isSelected = selected == value
                if (isSelected) {
                    Button(onClick = { onSelected(value) }, shape = RoundedCornerShape(10.dp)) {
                        Text("$value")
                    }
                } else {
                    OutlinedButton(onClick = { onSelected(value) }, shape = RoundedCornerShape(10.dp)) {
                        Text("$value")
                    }
                }
            }
        }
    }
}
