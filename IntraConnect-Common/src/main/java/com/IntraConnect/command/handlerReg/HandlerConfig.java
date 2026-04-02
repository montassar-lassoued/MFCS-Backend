package com.IntraConnect.command.handlerReg;

import com.IntraConnect.command.handlerReg.time.TriggerTime;
import com.IntraConnect.intf.Handler;

public record HandlerConfig(Class<? extends Handler<?>>  handler, TriggerTime time) {
}