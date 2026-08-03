package com.insaner.fonecheck.ui.screens.runall

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.CategoryTestResult
import com.insaner.fonecheck.domain.model.TestResult
import com.insaner.fonecheck.domain.model.TestSession
import com.insaner.fonecheck.domain.model.TestStatus
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.StandardCard
import com.insaner.fonecheck.ui.components.StatusBadge
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral400
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.Yellow400

@Composable
fun RunAllResultsScreen(
    session: TestSession,
    onOpenCategory: (Any) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val attentionResults =
        session.categories.filter {
            it.status is TestStatus.Fail || it.status is TestStatus.Warning
        }
    val completedResults =
        session.categories.filter {
            it.status == TestStatus.Pass || it.status is TestStatus.Info
        }
    val incompleteResults =
        session.categories.filter {
            it.status == TestStatus.NotAvailable || it.status == TestStatus.NotTested
        }
    var expandedCategoryName by rememberSaveable(attentionResults) {
        mutableStateOf(attentionResults.firstOrNull()?.category?.name)
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ResultsSummaryCard(session)
        }

        if (attentionResults.isNotEmpty()) {
            item {
                ResultSectionTitle(
                    title = stringResource(R.string.run_all_needs_attention),
                    count = attentionResults.size,
                )
            }
            categoryResultItems(
                results = attentionResults,
                expandedCategoryName = expandedCategoryName,
                onExpandedChange = { expandedCategoryName = it },
                onOpenCategory = onOpenCategory,
            )
        }

        if (completedResults.isNotEmpty()) {
            item {
                ResultSectionTitle(
                    title = stringResource(R.string.run_all_completed),
                    count = completedResults.size,
                )
            }
            item {
                CompletedResultsCard(
                    results = completedResults,
                    expandedCategoryName = expandedCategoryName,
                    onToggle = { result ->
                        expandedCategoryName = toggleExpanded(expandedCategoryName, result)
                    },
                    onOpenCategory = onOpenCategory,
                )
            }
        }

        if (incompleteResults.isNotEmpty()) {
            item {
                ResultSectionTitle(
                    title = stringResource(R.string.run_all_not_completed),
                    count = incompleteResults.size,
                )
            }
            categoryResultItems(
                results = incompleteResults,
                expandedCategoryName = expandedCategoryName,
                onExpandedChange = { expandedCategoryName = it },
                onOpenCategory = onOpenCategory,
            )
        }

        item {
            Button(
                onClick = onDone,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 20.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(stringResource(R.string.run_all_done))
            }
        }
    }
}

private fun LazyListScope.categoryResultItems(
    results: List<CategoryTestResult>,
    expandedCategoryName: String?,
    onExpandedChange: (String?) -> Unit,
    onOpenCategory: (Any) -> Unit,
) {
    items(
        items = results,
        key = { it.category.name },
    ) { result ->
        ExpandedCategoryResult(
            result = result,
            isExpanded = expandedCategoryName == result.category.name,
            onToggle = {
                onExpandedChange(toggleExpanded(expandedCategoryName, result))
            },
            onOpenCategory = onOpenCategory,
        )
    }
}

private fun toggleExpanded(
    currentCategoryName: String?,
    result: CategoryTestResult,
): String? = if (currentCategoryName == result.category.name) null else result.category.name

@Composable
private fun ResultsSummaryCard(session: TestSession) {
    val passed = session.categories.count { it.status == TestStatus.Pass }
    val warnings = session.categories.count { it.status is TestStatus.Warning }
    val failed = session.categories.count { it.status is TestStatus.Fail }
    val unavailable =
        session.categories.count {
            it.status == TestStatus.NotAvailable || it.status == TestStatus.NotTested
        }
    val scoreColor =
        when {
            session.overallScore >= 85 -> Green400
            session.overallScore >= 65 -> Yellow400
            else -> Red400
        }

    StandardCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.run_all_results_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.run_all_results_description),
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.overallScore.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = scoreColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.run_all_overall_score),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SummaryCount(
                        pluralStringResource(R.plurals.run_all_passed_count, passed, passed),
                        Green400,
                    )
                    SummaryCount(
                        pluralStringResource(R.plurals.run_all_warning_count, warnings, warnings),
                        Yellow400,
                    )
                    SummaryCount(
                        pluralStringResource(R.plurals.run_all_failed_count, failed, failed),
                        Red400,
                    )
                    SummaryCount(
                        pluralStringResource(
                            R.plurals.run_all_unavailable_count,
                            unavailable,
                            unavailable,
                        ),
                        Neutral400,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCount(
    text: String,
    color: Color,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = color,
    )
}

@Composable
private fun ResultSectionTitle(
    title: String,
    count: Int,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExpandedCategoryResult(
    result: CategoryTestResult,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOpenCategory: (Any) -> Unit,
) {
    val destination = diagnosticDestinations.first { it.category == result.category }
    CategoryResultCard(
        result = result,
        title = stringResource(destination.labelResId),
        imageResId = destination.imageResId,
        isExpanded = isExpanded,
        onToggle = onToggle,
        onOpen = { onOpenCategory(destination.route) },
    )
}

@Composable
private fun CompletedResultsCard(
    results: List<CategoryTestResult>,
    expandedCategoryName: String?,
    onToggle: (CategoryTestResult) -> Unit,
    onOpenCategory: (Any) -> Unit,
) {
    StandardCard {
        results.forEachIndexed { index, result ->
            val destination = diagnosticDestinations.first { it.category == result.category }
            CompactResultRow(
                result = result,
                title = stringResource(destination.labelResId),
                imageResId = destination.imageResId,
                isExpanded = expandedCategoryName == result.category.name,
                onToggle = { onToggle(result) },
                onOpen = { onOpenCategory(destination.route) },
            )
            if (index < results.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun CompactResultRow(
    result: CategoryTestResult,
    title: String,
    imageResId: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(14.dp),
    ) {
        ResultHeader(
            result = result,
            title = title,
            imageResId = imageResId,
            showSummary = false,
            imageSize = 44.dp,
        )
        if (isExpanded) {
            ResultDetails(result.results, onOpen)
        }
    }
}

@Composable
private fun CategoryResultCard(
    result: CategoryTestResult,
    title: String,
    imageResId: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
) {
    StandardCard(
        onClick = onToggle,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ResultHeader(
                result = result,
                title = title,
                imageResId = imageResId,
                showSummary = true,
                imageSize = 52.dp,
            )
            Text(
                text =
                    stringResource(
                        if (isExpanded) R.string.run_all_hide_details else R.string.run_all_view_details,
                    ),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (isExpanded) {
                ResultDetails(result.results, onOpen)
            }
        }
    }
}

@Composable
private fun ResultHeader(
    result: CategoryTestResult,
    title: String,
    imageResId: Int,
    showSummary: Boolean,
    imageSize: Dp,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(imageResId),
            contentDescription = null,
            modifier = Modifier.size(imageSize),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            if (showSummary) {
                Text(
                    text = result.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        StatusBadge(
            text = statusLabel(result.status),
            color = statusColor(result.status),
        )
    }
}

@Composable
private fun ResultDetails(
    results: List<TestResult>,
    onOpen: () -> Unit,
) {
    Column {
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        results.forEach { testResult ->
            ResultDetailRow(testResult)
        }
        OutlinedButton(
            onClick = onOpen,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(stringResource(R.string.run_all_open_test))
        }
    }
}

@Composable
private fun ResultDetailRow(result: TestResult) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            result.detail?.let { detail ->
                Text(
                    text = detail,
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        StatusBadge(
            text = statusLabel(result.status),
            color = statusColor(result.status),
        )
    }
}

@Composable
private fun statusLabel(status: TestStatus): String =
    stringResource(
        when (status) {
            TestStatus.Pass -> R.string.run_all_status_pass
            is TestStatus.Warning -> R.string.run_all_status_warning
            is TestStatus.Fail -> R.string.run_all_status_fail
            is TestStatus.Info -> R.string.run_all_status_info
            TestStatus.NotAvailable -> R.string.run_all_status_unavailable
            TestStatus.NotTested -> R.string.run_all_status_not_tested
        },
    )

@Composable
private fun statusColor(status: TestStatus): Color =
    when (status) {
        TestStatus.Pass -> Green400
        is TestStatus.Warning -> Yellow400
        is TestStatus.Fail -> Red400
        is TestStatus.Info -> MaterialTheme.colorScheme.primary
        TestStatus.NotAvailable, TestStatus.NotTested -> Neutral400
    }
