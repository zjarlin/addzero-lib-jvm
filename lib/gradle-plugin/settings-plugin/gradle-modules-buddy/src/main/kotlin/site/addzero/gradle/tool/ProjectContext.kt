package site.addzero.gradle.tool

import java.io.File

data class ProjectContext(
  val buildLogics: List<File>,
  val modules: List<File>,
  val blackModules: List<File>,
)
