package com.IntraConnect.command.handlerReg.time;

public record OneTimeTrigger (long delaySeconds) implements TriggerTime {
}
