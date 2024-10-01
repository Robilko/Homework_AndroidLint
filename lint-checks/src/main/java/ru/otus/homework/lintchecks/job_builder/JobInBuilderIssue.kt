package ru.otus.homework.lintchecks.job_builder

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity

@Suppress("UnstableApiUsage")
object JobInBuilderIssue {
    private const val ID = "JobInBuilderUsage"
    const val BRIEF_DESCRIPTION = "Job or SupervisorJob use in coroutine builder is not allowed."
    private const val EXPLANATION =
        "Частая ошибка при использовании корутин - передача экземпляра `Job`/`SupervisorJob` в корутин билдер. Хоть `Job` и его наследники являются элементами `CoroutineContext`, их использование внутри корутин-билдеров не имеет никакого эффекта, это может сломать ожидаемые обработку ошибок и механизм отмены корутин. Использование еще одного наследника `Job` - `NonCancellable` внутри корутин-билдеров сломает обработку ошибок у всех корутин в иерархии"
    private const val PRIORITY = 6

    val ISSUE = Issue.create(
        id = ID,
        briefDescription = BRIEF_DESCRIPTION,
        explanation = EXPLANATION,
        category = Category.LINT,
        priority = PRIORITY,
        severity = Severity.WARNING,
        implementation = Implementation(JobInBuilderDetector::class.java, Scope.JAVA_FILE_SCOPE)
    )
}