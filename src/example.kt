interface Producer<out T> {
    fun produce(): T
}

interface Consumer<in T> {
    fun consume(t: T): Unit
}

fun demo(
    producer: Producer<Any>,
    producerChild: Producer<String>,
    consumer: Consumer<Any>,
    consumerChild: Consumer<String>
) {

    /*
    변성에 대해 생각하는 순서.
    List<Object> parent;
    List<String> child;

    가 있다고 할 때
    parent = child
    child = parent

    원래 이런식의 할당이 안된다. (적어도 parent = child 는 될 줄 알았는데 그마저도..)

    근데, 가능하게 해주는 조건이 있다.

    List(감싸는 타입)가 T(감싸지는 타입)에 대해서

    1. producer 든지
    2. consumer 든지

    1. producer
      - interface LisT<T> {
          fun produce(): T
        }

      child = parent 를 하면.. 변수는 child 이므로 produce()의 타입은 자식타입(String)인데, 실제 참조객체는 parent의 로직을 사용하므로 parent.produce() 로직 내부에서
      downcasting => (String) Object 를 하면서 문제가 날 수 있다.
      그래서
      parent = child 만 허용한다.

      parent.produce(): Object => child.produce() => (Object) String => 문제 없음!

    2. consumer
      - interface List<T> {
          fun add(elem: T): Unit
        }

      parent = child 를 하면.. 변수는 parent 이므로 parent.add({Object type elem}) 을 하면 부모타입의 변수가 child.add의 로직 안으로 들어가면서
      똑같이 downcasting될텐데  (String) Object 문제가 날 수 있다.

      그래서
      child = parent 만 허용한다.

      child.add({String type elem}) => elem => parent.add(elem) => (Object) String => 문제 없음!

     */
    var p = producer
    var pc = producerChild
    var c = consumer
    var cc = consumerChild

//    pc = p // not possible
    p = pc // possible

//    c = cc // not possible
    cc = c // possible

    val cd: Consumer<Double> = consumer
}

typealias IntBinOp = (Int) -> (Int) -> Int

val add: IntBinOp = { a -> { b -> a + b } }

val doubleThenIncrement: (Int) -> Int = { x ->
    (x * 2) + 1
}

val double: (Int) -> Int = { it * 2 }
val increment: (Int) -> Int = { it + 1 }

//fun compose(
//    f1: (Int) -> Int,
//    f2: (Int) -> Int
//): (Int) -> Int = { x ->
//    f1(f2(x))
//}

typealias IntOp = (Int) -> Int

//fun ((Int) -> Int).compose(f: (Int) -> Int): (Int) -> Int = { i ->
//    this(f(i))
//}

fun <A, B, C> myCompose(g: (A) -> B, f: (B) -> C): (A) -> C = {
    f(g(it))
}

typealias MyFunction<A, B> = (A) -> B

infix fun <A, B, C> MyFunction<A, B>.compose(f: (B) -> C): (A) -> C = {
    f(this(it))
}

fun <A, B, C> curryExampleFunction(x: A, f: (A) -> (B) -> C): (B) -> C = f(x)

class TaxComputer(val rate: Double) : (Double) -> Double {
    override fun invoke(price: Double): Double = price * this.rate + price
    fun addTax(rate: Double): TaxComputer = TaxComputer(rate + this.rate)
    operator fun plus(tc: TaxComputer): TaxComputer = TaxComputer(this.rate + tc.rate)
}

fun main() {
    val price = TaxComputer(0.09).addTax(0.01)(12.0)
    val price2 = (TaxComputer(0.09) + TaxComputer(0.01))(12.0)
    println(add(3)(5))
    val aa: (Int) -> Int = { it + 5 }
    val bb: (Int) -> Int = { it * 2 }
    println(aa.compose(bb)(10))

    val square = { i: Int -> i * i }
    val triple = { i: Int -> i * 3 }
    val squareTriple = square compose triple
    println(squareTriple(5))
//    val cc = { i: Int -> i + 5 }.compose(aa)
//    println(compose({ it * 2 }, { it + 1 })(4))
}