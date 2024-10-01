package ru.otus.homework.lintchecks.job_builder


import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.kotlin.KotlinUBinaryExpression
import ru.otus.homework.lintchecks.hasArtifact
import ru.otus.homework.lintchecks.hasParent
import ru.otus.homework.lintchecks.job_builder.JobInBuilderIssue.ASYNC
import ru.otus.homework.lintchecks.job_builder.JobInBuilderIssue.BRIEF
import ru.otus.homework.lintchecks.job_builder.JobInBuilderIssue.COROUTINE_CONTEXT
import ru.otus.homework.lintchecks.job_builder.JobInBuilderIssue.COROUTINE_SCOPE
import ru.otus.homework.lintchecks.job_builder.JobInBuilderIssue.ISSUE
import ru.otus.homework.lintchecks.job_builder.JobInBuilderIssue.JOB
import ru.otus.homework.lintchecks.job_builder.JobInBuilderIssue.LAUNCH
import ru.otus.homework.lintchecks.job_builder.JobInBuilderIssue.NON_CANCELABLE
import ru.otus.homework.lintchecks.job_builder.JobInBuilderIssue.SUPERVISOR_JOB

@Suppress("UnstableApiUsage")
class JobInBuilderDetector : Detector(), Detector.UastScanner {

    override fun getApplicableMethodNames(): List<String> = listOf(LAUNCH, ASYNC)

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val receiver = context.evaluator.getTypeClass(node.receiverType)

        val isPerformOnCoroutineScope =
            context.evaluator.inheritsFrom(receiver, COROUTINE_SCOPE, false)
        if (!isPerformOnCoroutineScope) return

        node.valueArguments.forEach { arg ->
            val param = context.evaluator.getTypeClass(arg.getExpressionType())
            val isContainJob = context.evaluator.inheritsFrom(param, JOB, false) ||
                    (arg is KotlinUBinaryExpression &&
                            context.evaluator.inheritsFrom(param, COROUTINE_CONTEXT, false))

            if (isContainJob) {
                context.report(
                    issue = ISSUE,
                    scope = node,
                    location = context.getLocation(arg),
                    message = BRIEF,
                    quickfixData = createFix(context, arg, node)
                )
            }
        }
    }

    private fun createFix(
        context: JavaContext,
        node: UExpression,
        parentNode: UExpression
    ): LintFix? {
        val psiClass = node.getContainingUClass()?.javaPsi

        val param = context.evaluator.getTypeClass(node.getExpressionType())
        val isSupervisorJob = context.evaluator.inheritsFrom(
            cls = param,
            className = SUPERVISOR_JOB,
            strict = false
        )

        if (psiClass?.hasParent(context, "androidx.lifecycle.ViewModel") == true &&
            isSupervisorJob
        ) return createSupervisorJobFix(context, node)

        val isCoroutineContextWithOperator = node is KotlinUBinaryExpression &&
                context.evaluator.inheritsFrom(
                    cls = param,
                    className = COROUTINE_CONTEXT,
                    strict = false
                )

        if (isCoroutineContextWithOperator) {
            (node as KotlinUBinaryExpression).operands.forEach { expression ->
                val isSupervisorJobExpr = context.evaluator.inheritsFrom(
                    cls = context.evaluator.getTypeClass(expression.getExpressionType()),
                    className = SUPERVISOR_JOB,
                    strict = false
                )
                if (isSupervisorJobExpr) return createSupervisorJobFix(context, expression)
            }
        }

        val isNonCancelableJob = context.evaluator.inheritsFrom(param, NON_CANCELABLE, false)

        if (isNonCancelableJob) return createNonCancelableJobFix(context, parentNode)

        return null
    }

    private fun createSupervisorJobFix(
        context: JavaContext,
        node: UExpression
    ): LintFix? {
        val hasViewModelArtifact = hasArtifact(
            context = context,
            artifactName = "androidx.lifecycle:lifecycle-viewmodel-ktx"
        )

        if (!hasViewModelArtifact) return null

        return fix()
            .replace()
            .range(context.getLocation(node))
            .all()
            .with("")
            .build()
    }

    private fun createNonCancelableJobFix(
        context: JavaContext,
        node: UExpression
    ): LintFix? {
        if (isInAnotherCoroutine(node)) {
            return fix()
                .replace()
                .range(context.getLocation(node))
                .text(LAUNCH)
                .with("withContext")
                .build()
        }

        return null
    }

    private fun isInAnotherCoroutine(node: UElement?): Boolean {
        return when (val parent = node?.uastParent) {
            is UCallExpression -> getApplicableMethodNames()
                .any { methodName -> methodName == parent.methodName }

            null -> false
            else -> return isInAnotherCoroutine(parent)
        }
    }
}