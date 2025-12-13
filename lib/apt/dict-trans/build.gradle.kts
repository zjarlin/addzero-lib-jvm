plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

// This is a parent module for APT-based dictionary translation
// It contains:
// - apt-dict-annotations: Annotation definitions
// - apt-dict-processor: APT processor implementation

dependencies {
    // Export all sub-modules
    api(project(":lib:apt:dict-trans:apt-dict-annotations"))
    api(project(":lib:apt:dict-trans:apt-dict-processor"))
}