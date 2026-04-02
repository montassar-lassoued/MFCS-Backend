package com.IntraConnect.command.handlerReg.time;

public record NumberRateTrigger(long intervalSeconds, int times) implements TriggerTime{
}
