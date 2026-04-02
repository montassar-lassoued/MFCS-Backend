package com.IntraConnect.command.handlerReg.time;

public record FixedRateTrigger(long intervalSeconds) implements TriggerTime {
}
