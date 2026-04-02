package com.IntraConnect.command.handlerReg.time;


public sealed interface TriggerTime permits DailyTrigger, FixedRateTrigger, NoTimeTrigger, NumberRateTrigger, OneTimeTrigger {
}
