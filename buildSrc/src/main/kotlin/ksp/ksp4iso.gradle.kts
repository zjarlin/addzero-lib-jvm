plugins {
    id("com.google.devtools.ksp")
}
ksp {
    arg("isomorphicPackageName", "com.addzero.generated.isomorphic")
    arg("iso2DataProviderPackage", "com.addzero.generated.forms.dataprovider")
    arg("formPackageName", "com.addzero.generated.forms")
    arg("isomorphicClassSuffix", "Iso")

}
