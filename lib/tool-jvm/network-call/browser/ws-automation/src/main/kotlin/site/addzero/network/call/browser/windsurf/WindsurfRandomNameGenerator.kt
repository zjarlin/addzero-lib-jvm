package site.addzero.network.call.browser.windsurf

import java.security.SecureRandom

object WindsurfRandomNameGenerator {

  private val random = SecureRandom()

  private val firstNames = listOf(
    "Alex",
    "Sam",
    "Taylor",
    "Jordan",
    "Casey",
    "Riley",
    "Morgan",
    "Avery",
    "Drew",
    "Jamie",
    "Cameron",
    "Reese",
    "Quinn",
    "Kai",
    "Noah",
    "Liam",
    "Mason",
    "Ethan",
    "Lucas",
    "Leo",
  )

  private val lastNames = listOf(
    "Smith",
    "Johnson",
    "Brown",
    "Taylor",
    "Anderson",
    "Thomas",
    "Jackson",
    "White",
    "Harris",
    "Martin",
    "Thompson",
    "Garcia",
    "Martinez",
    "Robinson",
    "Clark",
    "Lewis",
    "Lee",
    "Walker",
    "Hall",
    "Allen",
  )

  fun randomFirstName(): String = firstNames[random.nextInt(firstNames.size)]

  fun randomLastName(): String = lastNames[random.nextInt(lastNames.size)]

  fun randomFullName(): Pair<String, String> = randomFirstName() to randomLastName()
}
