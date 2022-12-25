package ipr_two.model

data class Request(
    val id: Long,
    //дельта времени появления между заявками
    val deltaFromLastRequest: Long,
    //прошло времени со старта системы до появления заявки
    val deltaFromEpoch: Long,
    //время появления заявки
    val issueTime: Long,
    //сколько времени готова простоять в очереди
    val impatientQueueLeavingTime: Long,
    //сколько находилась в очереди до попадания на обслуживание
    var queueWaitingTime: Long = 0L,
    //сколько находилась на обслуживании в системе
    var serviceWaitingTime: Long = 0L,
    //появилась при заполненной очереди и не пошла дальше
    var isFaceFullQueue: Boolean = false
)
