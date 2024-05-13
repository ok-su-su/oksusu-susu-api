package com.oksusu.susu.common.config.netty

import com.oksusu.susu.common.consts.TRACE_ID
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.slf4j.MDC
import java.util.UUID

@Sharable
class MdcChannelInboundHandler: ChannelInboundHandlerAdapter() {
    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        MDC.put(TRACE_ID, UUID.randomUUID().toString())
        super.handlerAdded(ctx)
    }
}
