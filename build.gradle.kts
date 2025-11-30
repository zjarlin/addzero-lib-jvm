plugins {
    id("site.addzero.buildlogic.version-buddy") version "2025.11.29"
//    id("site.addzero.buildlogic.publish-buddy") version "2025.11.29"
       alias(libs.plugins.addzeroPublishBuddyNew)
    alias(libs.plugins.kotlinJvm) apply false
}
