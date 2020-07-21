import java.io.IOException
import java.io.Serializable
import kotlin.Exception
import kotlin.NullPointerException

sealed class Result<out A> : Serializable {
    abstract fun <B> map(f: (A) -> B): Result<B>
    abstract fun <B> flatMap(f: (A) -> Result<B>): Result<B>
    abstract fun mapFailure(message: String): Result<A>
    abstract fun forEach(
        onSuccess: (A) -> Unit = {},
        onFailure: (RuntimeException) -> Unit = {},
        onEmpty: () -> Unit = {}
    )

    fun getOrElse(defaultValue: @UnsafeVariance A): A = when (this) {
        is Success -> this.value
        else -> defaultValue
    }

    fun getOrElse(defaultValue: () -> @UnsafeVariance A): A = when (this) {
        is Success -> this.value
        else -> defaultValue()
    }

    fun orElse(defaultValue: () -> Result<@UnsafeVariance A>): Result<A> = when (this) {
        is Success -> this
        else -> try {
            defaultValue()
        } catch (e: RuntimeException) {
            Failure<A>(e)
        } catch (e: Exception) {
            Failure<A>(RuntimeException(e))
        }
    }

    fun filter(p: (A) -> Boolean): Result<A> =
        filter("Condition not matched", p)

    fun filter(message: String, p: (A) -> Boolean): Result<A> = flatMap {
        if (p(it)) this
        else Result.failure(message)
    }

    fun exists(p: (A) -> Boolean): Boolean = map(p).getOrElse(false)

    internal object Empty : Result<Nothing>() {
        override fun <B> map(f: (Nothing) -> B): Result<B> = Empty
        override fun <B> flatMap(f: (Nothing) -> Result<B>): Result<B> = Empty
        override fun mapFailure(message: String): Result<Nothing> = Empty
        override fun forEach(
            onSuccess: (Nothing) -> Unit,
            onFailure: (RuntimeException) -> Unit,
            onEmpty: () -> Unit
        ) = onEmpty()

        override fun toString(): String = "Empty"
    }

    internal class Failure<out A>(private val exception: RuntimeException) : Result<A>() {
        override fun <B> map(f: (A) -> B): Result<B> = Failure(exception)
        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> = Failure(exception)
        override fun mapFailure(message: String): Result<A> = Failure(RuntimeException(message))
        override fun forEach(
            onSuccess: (A) -> Unit,
            onFailure: (RuntimeException) -> Unit,
            onEmpty: () -> Unit
        ) = onFailure(exception)

        override fun toString(): String = "Failure(${exception.message})"
    }

    internal class Success<out A>(internal val value: A) : Result<A>() {
        override fun <B> map(f: (A) -> B): Result<B> = try {
            Success(f(value))
        } catch (e: RuntimeException) {
            Failure(e)
        } catch (e: Exception) {
            Failure(RuntimeException(e))
        }

        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> = try {
            f(value)
        } catch (e: RuntimeException) {
            Failure(e)
        } catch (e: Exception) {
            Failure(RuntimeException(e))
        }

        override fun mapFailure(message: String): Result<A> = this
        override fun forEach(
            onSuccess: (A) -> Unit,
            onFailure: (RuntimeException) -> Unit,
            onEmpty: () -> Unit
        ) = onSuccess(value)

        override fun toString(): String = "Success($value)"
    }

    companion object {
        operator fun <A> invoke(a: A? = null): Result<A> = when (a) {
            null -> Failure(NullPointerException())
            else -> Success(a)
        }

        operator fun <A> invoke(): Result<A> = Empty

        fun <A> failure(message: String): Result<A> = Failure(IllegalStateException(message))
        fun <A> failure(exception: RuntimeException): Result<A> = Failure(exception)
        fun <A> failure(exception: Exception): Result<A> = Failure(IllegalStateException(exception))
    }
}

fun <K, V> Map<K, V>.getResult(key: K) = when {
    this.containsKey(key) -> Result(this[key])
    else -> Result.Empty
}

// overloading 테크닉 살펴보면 좋을듯.
data class Toon private constructor(
    val firstName: String,
    val lastName: String,
    val email: Result<String>
) {
    companion object {
        operator fun invoke(
            firstName: String,
            lastName: String
        ) = Toon(firstName, lastName, Result.Empty)

        operator fun invoke(
            firstName: String,
            lastName: String,
            email: String?
        ) = Toon(firstName, lastName, Result(email))
    }
}

fun getName(): Result<String> = try {
    validate(readLine())
} catch (e: Exception) {
    Result.failure(e)
}

fun validate(name: String?): Result<String> = when {
    name?.isNotEmpty() ?: false -> Result(name)
    else -> Result.failure(IOException())
}

fun main() {
    val toons: Map<String, Toon> =
        mapOf(
            "Mickey" to Toon("Mickey", "Mouse", "mickey@disney.com"),
            "Minnie" to Toon("Minnie", "Mouse"),
            "Donald" to Toon("Donald", "Duck", "donald@disney.com")
        )

    val toon = getName()
        .flatMap { toons.getResult(it) }
        .flatMap { it.email }
    println(toon)

    val maybeFirstName = Result("larry")
    val maybeLastName = Result("jung")
    val maybeEmail = Result(null)
    val createPerson: (String) -> (String) -> (String) -> Toon = { x ->
        { y ->
            { z ->
                Toon(x, y, z)
            }
        }
    }
    // for-comprehension 없이는 이런식으로 만들어야 한다.
    val toon1 = lift3(createPerson)(maybeFirstName)(maybeLastName)(maybeEmail)

    val toon_for_comprehension_1 =
        maybeFirstName.flatMap { firstName ->
            maybeLastName.flatMap { lastName ->
                maybeEmail.map { email ->
                    createPerson(firstName)(lastName)(email)
                }
            }
        }

    // kt-arrow 에서는 Monad comprehension을 제공해준다.
    // https://arrow-kt.io/docs/0.10/patterns/monad_comprehensions/
}

fun <A, B> lift(f: (A) -> B): (Result<A>) -> Result<B> = {
    it.map(f)
}

fun <A, B, C> lift2(f: (A) -> (B) -> C): (Result<A>) -> (Result<B>) -> Result<C> =
    { ra: Result<A> ->
        { rb: Result<B> ->
            ra.map { f(it) } // Result<(B) -> C>
                .flatMap { rb.map(it) }
        }
    }

fun <A, B, C, D> lift3(f: (A) -> (B) -> (C) -> D): (Result<A>) -> (Result<B>) -> (Result<C>) -> Result<D> =
    { ra ->
        { rb ->
            { rc ->
                ra.map(f) // Result<(B) -> (C) -> D>
                    .flatMap { rb.map(it) } // Result<(C) -> D>
                    .flatMap { rc.map(it) } // Result<D>
            }
        }
    }
