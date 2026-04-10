package com.IntraConnect.command.handlerReg.time;

public record FixedRateTrigger(long intervalMilliSeconds) implements TriggerTime {
}
