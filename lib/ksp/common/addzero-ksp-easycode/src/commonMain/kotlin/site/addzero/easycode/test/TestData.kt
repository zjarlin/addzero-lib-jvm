package site.addzero.easycode.test

data class TestData(
    val packageName: String,
    val className: String,
    val fields: List<TestField>
)

data class TestField(
    val name: String,
    val type: String
)