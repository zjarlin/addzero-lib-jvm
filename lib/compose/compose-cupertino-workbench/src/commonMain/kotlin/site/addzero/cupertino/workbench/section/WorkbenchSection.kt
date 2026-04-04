package site.addzero.cupertino.workbench.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.section.CupertinoSection
import io.github.robinpcrd.cupertino.section.SectionItem
import io.github.robinpcrd.cupertino.section.SectionScope
import io.github.robinpcrd.cupertino.section.SectionStyle

typealias WorkbenchSectionScope = SectionScope
typealias WorkbenchSectionStyle = SectionStyle

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun WorkbenchSection(
  modifier: Modifier = Modifier,
  style: WorkbenchSectionStyle = SectionStyle.InsetGrouped,
  content: @Composable WorkbenchSectionScope.() -> Unit,
) {
  CupertinoSection(
    modifier = modifier,
    style = style,
    content = content,
  )
}

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun WorkbenchSectionScope.WorkbenchSectionItem(
  modifier: Modifier = Modifier,
  title: @Composable () -> Unit,
  trailingContent: @Composable () -> Unit = {},
) {
  SectionItem(
    modifier = modifier,
    title = title,
    trailingContent = trailingContent,
  )
}
