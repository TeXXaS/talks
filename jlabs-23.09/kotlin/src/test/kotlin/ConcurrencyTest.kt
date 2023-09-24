import kotlinx.coroutines.*
import org.junit.Test
import java.util.concurrent.Executors

class ConcurrencyTest {
    @Test
    fun testHelloWorld() {
        for (i in 0..1) {
            GlobalScope.launch {
                delay(10L)
                val a = slowlyRespondingService(i)
                val b = slowlyRespondingService2(i)
                val c = slowlyRespondingService3(i)
                println("finishing iteration $i - $a $b $c")
            }
        }
        println("bum!")
        Thread.sleep(2000L)
    }

    private val dispatcher = Executors.newFixedThreadPool(10)
        .asCoroutineDispatcher()
//    @OptIn(ExperimentalCoroutinesApi::class)
//    private val dispatcher = Dispatchers.Default.limitedParallelism(10)
    private val dispatcher2 = Executors.newFixedThreadPool(10)
        .asCoroutineDispatcher()
    private val dispatcher3 = Executors.newFixedThreadPool(1)
        .asCoroutineDispatcher()

    @Test
    fun `something more complicated`() {
        for (i in 0..10) {
//            GlobalScope.launch {
            runBlocking {
                withTimeout(300) {
                    delay(10L)
                    val a = async(dispatcher) { slowlyRespondingService(i) }
                    val b = async(dispatcher2) { slowlyRespondingService2(i) }
                    val c = async(dispatcher3) { slowlyRespondingService3(i) }
                    println("finishing iteration $i - ${a.await()} ${b.await()} ${c.await()}")
                }
            }
        }
        println("bum!")
        Thread.sleep(2000L)
    }

    private suspend fun slowlyRespondingService(iteration: Int): Int {
        try {
            println("slow service called $iteration")
            delay(500L)
            println("slow service responding $iteration")
            return 10
        } catch (e: TimeoutCancellationException) {
            println("slow service got cancelled $iteration")
            throw e
        }
    }

    private suspend fun slowlyRespondingService2(iteration: Int): Int {
        try {
            println("slow service 2 called $iteration")
            delay(500L)
            println("slow service 2 responding $iteration")
            return 20
        } catch (e: TimeoutCancellationException) {
            println("slow service 2 got cancelled $iteration")
            throw e
        }
    }

    private suspend fun slowlyRespondingService3(iteration: Int): Int {
        try {
            println("slow service 3 called $iteration")
            delay(400L)
            println("slow service 3 responding $iteration")
            return 30
        } catch (e: TimeoutCancellationException) {
            println("slow service 3 got cancelled $iteration")
            throw e
        } finally {
            withContext(NonCancellable) {
                println("slow service 3 spending time on cleaning resources $iteration")
                delay(200L)
            }
        }
    }
}
