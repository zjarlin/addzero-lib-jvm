plugins {
    `java-library`
//    `kotlin-convention`
}
dependencies{
   implementation(libs.hutool.all)
    implementation("com.baomidou:mybatis-plus-core:3.5.12")
//    implementation("com.baomidou:mybatis-plus:3.5.12")
    implementation("org.apache.commons:commons-lang3:3.18.0")
}
