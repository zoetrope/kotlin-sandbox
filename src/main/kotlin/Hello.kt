fun main(args: List<String>) {
    val h = Hello();
    h.hello()
}

class Hello {
    fun hello() {
        println("Hello World!");
    }

    fun calc(a: Int, b: Int): Int = a + b
}