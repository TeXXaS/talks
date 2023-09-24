import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import org.junit.Test
import java.util.*


class ChannelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.produceUUIDs() = produce {
        while (true) {
            delay(1000)
            val uuid = UUID.randomUUID().toString()
            send(uuid)
        }
    }

    private fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<String>) = launch {
        for (msg in channel) {
            println("Processor #$id received $msg")
        }
    }

    @Test
    fun main() = runBlocking {
        val chan = produceUUIDs()
        repeat(5) { launchProcessor(it, chan) }
        delay(19999)
        chan.cancel()
    }
}
