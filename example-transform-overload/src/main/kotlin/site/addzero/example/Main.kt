package site.addzero.example

fun main() {
  val methods = KSaveCommandCreator::class.java.methods
  val generated = methods
        .firstOrNull { method -> method.name == "saveEntitiesCommandViaToEntityInput" }

    println(generated)
}
