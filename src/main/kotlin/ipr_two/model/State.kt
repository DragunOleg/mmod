package ipr_two.model

data class State(
    //сколько сейчас в очереди
    val queueSize: Int,
    //сколько сейчас занято каналов
    val busyChannels: Int,
    //сколько времени провисело это состояние, прежде чем сменилось на новое
    var stateTime: Long,
    // systemtimemillis на момент перехода системы в это состояние
    val stateTimeStamp: Long,
    //сколько уже ушло из очереди упершись в максимум
    val queueLeftSize: Int,
    //сколько уже обслужено
    val finishedRequests: Int,
    //сколько нетерпеливо ушло из очереди
    val impatientLeftSize: Int
)
