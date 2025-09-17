import util.getProjectDirConfigMap

plugins {
    id("com.google.devtools.ksp")
}

ksp {
    getProjectDirConfigMap().forEach { (key, value) ->
        arg(key, value)
    }
}
