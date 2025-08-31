import org.junit.jupiter.api.Test

class Jvm测试 {

    @Test
    fun test1() {
//        val message = System.getenv("SERVER_HOST")
        val message = System.getenv("user.dir")
        val getenv = System.getenv("user.home")
        println(message)

    }
}
