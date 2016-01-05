import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

class HelloSpecs: Spek() { init {

    given("create Hello instance") {
        val hello = Hello()
        on("calculate") {
            val value = hello.calc(1, 2)
            it("should result in a value of 3") {
                assertEquals(5, value)
            }
        }
    }
}}