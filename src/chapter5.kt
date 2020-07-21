sealed class List<A> {
    abstract fun isEmpty(): Boolean
    fun cons(a: A): List<A> = Cons(a, this)
    fun setHead(a: A): List<A> = when (this) {
        Nil -> throw IllegalStateException("setHead called on an empty list")
        is Cons -> tail.cons(a)
    }

    abstract fun drop(n: Int): List<A>

    private object Nil : List<Nothing>() {
        override fun isEmpty(): Boolean = true
        override fun toString(): String = "[NIL]"
        override fun drop(n: Int): List<Nothing> = this
    }

    private class Cons<A>(
        internal val head: A,
        internal val tail: List<A>
    ) : List<A>() {
        override fun isEmpty(): Boolean = false
        override fun toString(): String = "[${toString("", this)}NIL]"
        private tailrec fun toString(acc: String, list: List<A>): String =
            when (list) {
                is Nil -> acc
                is Cons -> toString("$acc${list.head},", list.tail)
            }

        override fun drop(n: Int): List<A> =
            if (n == 0) this else tail.drop(n - 1)
    }

    companion object {
        operator fun <A> invoke(vararg az: A): List<A> = az.foldRight(Nil as List<A>) { a: A, list: List<A> ->
            Cons(a, list)
        }
    }
}