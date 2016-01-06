import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.functional.bind
import nl.komponents.kovenant.functional.map
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.thenUse

fun main(args: Array<String>) {
    val h = Hello();
    //    h.asyncHello()
    h.nestedPromise()
}

class Hello {
    fun hello() {
        println("Hello World!");
    }

    fun asyncHello() {
        task { "world" } and task { "Hello" } success {
            println("${it.second} ${it.first}?")
        }
    }

    fun nestedPromise() {
        val p1 = task {
            Thread.sleep(2000)
            "Hello"
        }
        val p2 = task {
            Thread.sleep(1000)
            "World"
        }

        p1 bind {
            println(it)
            p2
        } success {
            println(it)
        }

        p1 bind {
            p2 thenUse {
                it to this
            }
        } success {
            println(it)
        }

        println("finish")
    }

    fun calc(a: Int, b: Int): Int = a + b
}