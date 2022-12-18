package ipt_two.model

//todo сделать генерацию критического времени ухода, по истечению которого заявка уходит из очереди
//Это можно сделать тут, добавив к issueTime критическую дельту
data class Request(
    val id: Long,
    //дельта времени появления между заявками
    val deltaFromLastRequest: Long,
    //прошло времени со старта системы до появления заявки
    val deltaFromEpoch: Long,
    //время появления заявки
    val issueTime: Long,
    //сколько находилась в очереди до попадания в систему
    var queueWaitingTime: Long = 0L,
    //сколько находилась на обслуживании в системе
    var serviceWaitingTime: Long = 0L,
    //появилась при заполненной очереди и не пошла дальше
    var isFaceFullQueue: Boolean = false
)
