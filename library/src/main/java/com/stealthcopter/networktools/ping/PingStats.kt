package com.stealthcopter.networktools.ping

import java.net.InetAddress

class PingStats(
    val address: InetAddress,
    val noPings: Long,
    val packetsLost: Long,
    totalTimeTaken: Float,
    minTimeTaken: Float,
    maxTimeTaken: Float
) {
    val averageTimeTaken: Float
    val minTimeTaken: Float
    val maxTimeTaken: Float
    val isReachable: Boolean

    init {
        averageTimeTaken = totalTimeTaken / noPings
        this.minTimeTaken = minTimeTaken
        this.maxTimeTaken = maxTimeTaken
        isReachable = noPings - packetsLost > 0
    }

    val averageTimeTakenMillis: Long
        get() = (averageTimeTaken * 1000).toLong()
    val minTimeTakenMillis: Long
        get() = (minTimeTaken * 1000).toLong()
    val maxTimeTakenMillis: Long
        get() = (maxTimeTaken * 1000).toLong()

    override fun toString(): String {
        return "PingStats{" +
                "ia=" + address +
                ", noPings=" + noPings +
                ", packetsLost=" + packetsLost +
                ", averageTimeTaken=" + averageTimeTaken +
                ", minTimeTaken=" + minTimeTaken +
                ", maxTimeTaken=" + maxTimeTaken +
                '}'
    }
}