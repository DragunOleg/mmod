package ipt_two.model

data class State(
    //сколько сейчас в очереди
    val queueSize: Int,
    //сколько сейчас занято каналов
    val busyChannels: Int,
    //сколько времени провисело это состояние, прежде чем сменилось на новое
    var stateTime: Long,
    // systemtimemillis на момент перехода системы в это состояние
    val stateTimeStamp: Long
)
