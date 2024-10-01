package ru.otus.homework.lintchecks.color_usage

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity

@Suppress("UnstableApiUsage")
object ColorUsageIssue {
    private const val ID = "RawColorUsage"
    const val BRIEF_DESCRIPTION = "Используемые цвета должны браться из палитры."
    private const val EXPLANATION =
        "Все цвета, которые используются в ресурсах приложения должны находится в палитре. За палитру следует принимать цвета, описанные в файле `colors.xml`"
    private const val PRIORITY = 6

    val ISSUE = Issue.create(
        id = ID,
        briefDescription = BRIEF_DESCRIPTION,
        explanation = EXPLANATION,
        category = Category.LINT,
        priority = PRIORITY,
        severity = Severity.WARNING,
        implementation = Implementation(
            ColorUsageDetector::class.java,
            Scope.RESOURCE_FILE_SCOPE
        )
    )
}