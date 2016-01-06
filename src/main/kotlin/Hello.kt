import nl.komponents.kovenant.*
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.functional.bind
import nl.komponents.kovenant.functional.map
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.transport.TransportAddress
import org.elasticsearch.index.query.QueryBuilders
import rx.Observable
import rx.Subscriber
import rx.lang.kotlin.AsyncSubject
import rx.lang.kotlin.deferredObservable
import rx.lang.kotlin.fold
import rx.lang.kotlin.observable
import java.net.InetAddress

fun main(args: Array<String>) {
    val h = Hello();
    //    h.asyncHello()
    //h.obs()
    //h.promiseToObservable()
    h.es()
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

    fun es() {
        val client = esClient(listOf(InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))) {
            settings(esSettings {
                put("cluster.name", "elasticsearch")
            })
        }

        client.searchAsync("*") {
            setSize(10)
            setQuery(QueryBuilders.matchAllQuery())
        } success {
            it.hits?.hits?.forEach {
                println(it.source)
            }
        }
        Thread.sleep(5000)
    }

    fun esSettings(block: Settings.Builder.() -> Unit): Settings {
        val settings = Settings.settingsBuilder()
        settings.block()
        return settings.build()
    }

    fun esClient(nodes: List<TransportAddress>, block: TransportClient.Builder.() -> Unit): TransportClient {
        val builder = TransportClient.builder()
        builder.block()
        val client = builder.build()
        nodes.forEach {
            client.addTransportAddress(it)
        }
        return client
    }

    fun TransportClient.searchAsync(vararg indices: String, block: SearchRequestBuilder.() -> Unit): Promise<SearchResponse, Throwable> {

        val deferred = deferred<SearchResponse, Throwable>();
        val builder = this.prepareSearch(*indices)
        builder.block()

        builder.execute(object : ActionListener<SearchResponse> {
            override fun onResponse(res: SearchResponse) {
                deferred.resolve(res)
            }

            override fun onFailure(e: Throwable) {
                deferred.reject(e)
            }
        })

        return deferred.promise
    }

}