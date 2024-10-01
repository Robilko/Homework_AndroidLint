package ru.otus.homework.lintchecks.color_usage

import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScannerConstants
import org.w3c.dom.Attr
import org.w3c.dom.Element
import ru.otus.homework.lintchecks.color_usage.ColorUsageIssue.BRIEF_DESCRIPTION
import ru.otus.homework.lintchecks.color_usage.ColorUsageIssue.ISSUE

@Suppress("UnstableApiUsage")
class ColorUsageDetector : ResourceXmlDetector() {

    private val rawColors = mutableListOf<RawColor>()
    private val paletteColors = mutableMapOf<String, String>()

    override fun getApplicableAttributes(): Collection<String>? {
        return XmlScannerConstants.ALL
    }

    override fun getApplicableElements(): Collection<String> {
        return XmlScannerConstants.ALL
    }

    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        val attrValue = attribute.value.orEmpty()
        if (attrValue.isRawColor()) {
            rawColors.add(
                RawColor(
                    location = context.getValueLocation(attribute),
                    color = attrValue.lowercase()
                )
            )
        }
    }

    override fun visitElement(context: XmlContext, element: Element) {
        if (context.file.name == PALETTE_FILE) {
            val colorName = element.attributes.item(0)?.nodeValue?.lowercase() ?: return
            val colorCode = element.firstChild.nodeValue.lowercase()
            paletteColors[colorCode] = colorName
        }
    }

    override fun afterCheckRootProject(context: Context) {
        rawColors.forEach { rawColor ->
            val colorNameToReplace = paletteColors[rawColor.color]
            val location = rawColor.location
            val fix = colorNameToReplace?.let {
                createColorFix(location = location, newColor = it)
            }
            context.report(
                issue = ISSUE,
                location = location,
                message = BRIEF_DESCRIPTION,
                quickfixData = fix
            )
        }
    }

    private fun createColorFix(location: Location, newColor: String): LintFix {
        return fix()
            .replace()
            .range(location)
            .all()
            .with(COLOR_PREFIX + newColor)
            .build()
    }

    private fun String.isRawColor(): Boolean {
        return this.matches("^#([a-fA-F0-9]{3}|[a-fA-F0-9]{6}|[a-fA-F0-9]{8})$".toRegex())
    }

    data class RawColor(
        val location: Location,
        val color: String
    )

    private companion object {
        const val PALETTE_FILE = "colors.xml"
        const val COLOR_PREFIX = "@color/"
    }
}