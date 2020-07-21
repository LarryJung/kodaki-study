import java.math.BigInteger

fun sum(n: Int): Int {
    tailrec fun sum(s: Int, i: Int): Int =
        if (i > n) s
        else sum(s + i, i + 1)
    return sum(0, 0)
}

fun inc(n: Int) = n + 1
fun dec(n: Int) = n - 1
fun add(a: Int, b: Int): Int {
    tailrec fun addHelper(x: Int, y: Int): Int =
        if (y == 0) x
        else addHelper(inc(x), dec(y))
    return addHelper(a, b)
}

fun fibonacci(number: Int): Int =
    if (number == 0 || number == 1) 1
    else fibonacci(number - 1) + fibonacci(number - 2)

// 어렵네..ㅎ
fun fib(x: Int): BigInteger {
    tailrec fun fib(val1: BigInteger, val2: BigInteger, x: BigInteger): BigInteger =
        when {
            (x == BigInteger.ZERO) -> BigInteger.ONE
            (x == BigInteger.ONE) -> val1 + val2
            else -> fib(val2, val1 + val2, x - BigInteger.ONE)
        }
    return fib(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(x.toLong()))
}

fun <T> makeString(list: List<T>): String {
    val delim = ","
    fun go(l: List<T>, s: String): String =
        when {
            l.isEmpty() -> s
            l.drop(1).isEmpty() -> "${s}${l.first()}${delim}"
            else -> go(l.drop(1), "${s}${delim}${l.first()}")
        }
    return go(list, "")
}

fun <T, U> foldLeft(list: List<T>, i: U, f: (T, U) -> U): U {
    fun go(l: List<T>, acc: U): U =
        if (l.isEmpty()) acc
        else go(l.drop(1), f(l[0], acc))
    return go(list, i)
}

fun summ(list: List<Int>) = foldLeft(list, 0) { a, b -> a + b }
fun <T> toString(list: List<T>) = foldLeft(list, "") { a, b -> "${a}${b}" }

fun <T> reverse(list: List<T>) = foldLeft(list, listOf<T>()) { a, b ->
    listOf(a) + b
}

fun <T> prepend(list: List<T>, elem: T): List<T> =
    foldLeft(list, listOf(elem)) { lst, elem -> elem + listOf(lst) }

fun <T> iterate(n: Int, seed: T, f: (T) -> T): List<T> {
    fun iterateHelper(l: List<T>): List<T> =
        if (l.size == n) l
        else iterateHelper(l.plus(f(l.last())))
    return iterateHelper(listOf(seed))
}

fun <T, U> map(list: List<T>, f: (T) -> U): List<U> =
    foldLeft(list, listOf()) { t, list ->
        list + f(t)
    }

fun fiboCoRecursion(n: Int): List<Int> =
    map(iterate(n + 1, 0 to (1 to 1)) { it.first to (it.second.second to it.second.first + it.second.second) }) { pair ->
        if (pair.first % 2 == 0) pair.second.first
        else pair.second.second
    }

fun main() {
    println(sum(10))
    println(add(5, 10))
    println(reverse(listOf(1, 2, 3, 4, 5, 6, 7, 8)))
    println(prepend(listOf(1, 2), 3))

    println(
        iterate(10, 0 to (1 to 1)) { it.first to (it.second.second to it.second.first + it.second.second) }
    )

    println(
        map(listOf(1, 2, 3)) { it * 2 }
    )

    println(fiboCoRecursion(10))
}
