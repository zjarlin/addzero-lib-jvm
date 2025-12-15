plugins {
    kotlin("jvm")
}

dependencies {
    // Shared data models from APT module
    implementation(project(":lib:jimmer-apt-autoddl"))
    
    // LSI Framework dependencies
    implementation(project(":checkouts:metaprogramming-lsi:lsi-core"))
    implementation(project(":checkouts:metaprogramming-lsi:lsi-ksp"))
    
    // DDL Generator dependencies
    implementation(project(":checkouts:ddlgenerator:tool-ddlgenerator-core"))
    implementation(project(":checkouts:ddlgenerator:tool-ddlgenerator-parser"))
    implementation(project(":checkouts:ddlgenerator:tool-ddlgenerator-sql"))
    
    // Jimmer dependencies
    implementation("org.babyfish.jimmer:jimmer-sql:0.8.137")
    
    // KSP dependencies
    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.10-1.0.9")
    
    // Testing dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
    testImplementation("io.kotest:kotest-assertions-core:5.5.5")
    testImplementation("io.kotest:kotest-property:5.5.5")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}