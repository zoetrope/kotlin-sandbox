import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.task

fun main(args: Array<String>) {
    val h = Hello();
    h.asyncHello()
}

class Hello {
    fun hello() {
        println("Hello World!");
    }

    fun asyncHello(){
        task { "world" } and task { "Hello" } success {
            println("${it.second} ${it.first}?")
        }
    }

    fun calc(a: Int, b: Int): Int = a + b
}