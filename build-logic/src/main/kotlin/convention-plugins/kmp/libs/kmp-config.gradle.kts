import com.codingfeline.buildkonfig.compiler.FieldSpec
import util.KonfigUtil.getProjectProperties
import util.defByClass
import util.defByMap

plugins {
    id("com.codingfeline.buildkonfig")
}

buildkonfig {
    packageName = BuildSettings.PACKAGE_NAME
    defaultConfigs {
        defByClass(BuildSettings::class)
    }
    defaultConfigs {
        val projectProperties = getProjectProperties(project,".env.prod")
        defByMap(projectProperties)
    }

    defaultConfigs("dev") {
        buildConfigField(FieldSpec.Type.STRING, "dasda", "sss")
    }


}
