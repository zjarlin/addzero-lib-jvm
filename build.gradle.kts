plugins {
//    id("site.addzero.buildlogic.version-buddy") version "0.0.656"
    alias(libs.plugins.addzeroVersionBuddy)
    alias(libs.plugins.addzeroPublishBuddyNew)
//    alias(libs.plugins.addzeroPublishBuddy) apply false
    alias(libs.plugins.kotlinJvm) apply false
}
