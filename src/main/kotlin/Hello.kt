import nl.komponents.kovenant.*
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.functional.bind
import nl.komponents.kovenant.functional.map
import rx.Observable
import rx.Subscriber
import rx.lang.kotlin.AsyncSubject
import rx.lang.kotlin.deferredObservable
import rx.lang.kotlin.fold
import rx.lang.kotlin.observable

fun main(args: Array<String>) {
    val h = Hello();
    //    h.asyncHello()
    //h.obs()
    h.promiseToObservable()
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

    fun <T> Observable<T>.toPromise(): Promise<T, Throwable> {
        val deferred = deferred<T, Throwable>();
        this.subscribe({ x -> deferred.resolve(x) }, { e -> deferred.reject(e) })
        return deferred.promise;
    }

    fun <V> Promise<V, Throwable>.toObservable(): Observable<V> {
        val subject = AsyncSubject<V>()
        success {
            subject.onNext(it)
            subject.onCompleted()
        } fail {
            subject.onError(it)
        }
        return subject;
    }

    fun <T> observable2(body: Subscriber<in T>.() -> Unit): Observable<T> = Observable.create(body)

    fun obs() {
        observable2<String> {
            onNext("H")
            onNext("e")
            onNext("l")
            onNext("")
            onNext("l")
            onNext("o")
            onCompleted()
        }
                .filter { it.isNotEmpty() }
                .fold (StringBuilder()) { sb, e -> sb.append(e) }
                .map { it.toString() }
                .subscribe { result ->
                    print(result)
                }

    }

    fun obsToPromise() {
        observable<String> { subscriber ->
            subscriber.onNext("Observable to Promise")
            subscriber.onCompleted()
        }.toPromise() success{
            println(it)
        }
    }

    fun promiseToObservable() {
        task {
            "Promise to Observable"
        }.toObservable().subscribe({
            println(it)
        })
    }

}