# Multireceiver IDEA Plugin

Provides IDE support for `site.addzero.kcp.multireceiver`.

- Bundles the compiler plugin jar inside the IDEA plugin
- Enables Kotlin K2 community compiler plugins for the opened project
- Substitutes the Gradle-resolved compiler plugin jar with the bundled jar so generated wrappers can be analysed inside the IDE
