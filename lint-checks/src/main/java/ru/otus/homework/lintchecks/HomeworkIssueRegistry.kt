package ru.otus.homework.lintchecks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue
import ru.otus.homework.lintchecks.global_scope.GlobalScopeIssue
import ru.otus.homework.lintchecks.job_builder.JobInBuilderIssue

@Suppress("UnstableApiUsage")
class HomeworkIssueRegistry : IssueRegistry() {

    override val issues: List<Issue> = listOf(
        GlobalScopeIssue.ISSUE,
        JobInBuilderIssue.ISSUE
    )
}