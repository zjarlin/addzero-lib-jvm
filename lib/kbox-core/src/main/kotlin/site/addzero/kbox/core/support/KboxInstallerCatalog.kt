package site.addzero.kbox.core.support

import site.addzero.kbox.core.model.KboxInstallerPlatform
import site.addzero.kbox.core.model.KboxInstallerRule

object KboxInstallerCatalog {
    fun defaultRules(): List<KboxInstallerRule> {
        return listOf(
            KboxInstallerRule("exe", KboxInstallerPlatform.WINDOWS, "exe"),
            KboxInstallerRule("msi", KboxInstallerPlatform.WINDOWS, "msi"),
            KboxInstallerRule("msix", KboxInstallerPlatform.WINDOWS, "msix"),
            KboxInstallerRule("msixbundle", KboxInstallerPlatform.WINDOWS, "msixbundle"),
            KboxInstallerRule("appx", KboxInstallerPlatform.WINDOWS, "appx"),
            KboxInstallerRule("appxbundle", KboxInstallerPlatform.WINDOWS, "appxbundle"),
            KboxInstallerRule("dmg", KboxInstallerPlatform.MACOS, "dmg"),
            KboxInstallerRule("pkg", KboxInstallerPlatform.MACOS, "pkg"),
            KboxInstallerRule("mpkg", KboxInstallerPlatform.MACOS, "mpkg"),
            KboxInstallerRule("xip", KboxInstallerPlatform.MACOS, "xip"),
            KboxInstallerRule("deb", KboxInstallerPlatform.LINUX, "deb"),
            KboxInstallerRule("rpm", KboxInstallerPlatform.LINUX, "rpm"),
            KboxInstallerRule("appimage", KboxInstallerPlatform.LINUX, "appimage"),
            KboxInstallerRule("flatpak", KboxInstallerPlatform.LINUX, "flatpak"),
            KboxInstallerRule("flatpakref", KboxInstallerPlatform.LINUX, "flatpakref"),
            KboxInstallerRule("snap", KboxInstallerPlatform.LINUX, "snap"),
            KboxInstallerRule("run", KboxInstallerPlatform.LINUX, "run"),
            KboxInstallerRule("apk", KboxInstallerPlatform.ANDROID, "apk"),
            KboxInstallerRule("aab", KboxInstallerPlatform.ANDROID, "aab"),
            KboxInstallerRule("ipa", KboxInstallerPlatform.IOS, "ipa"),
        )
    }
}
