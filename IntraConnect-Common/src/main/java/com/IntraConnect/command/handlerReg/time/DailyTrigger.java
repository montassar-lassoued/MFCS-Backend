package com.IntraConnect.command.handlerReg.time;

import java.time.LocalTime;

public record DailyTrigger(LocalTime time) implements TriggerTime {
}
