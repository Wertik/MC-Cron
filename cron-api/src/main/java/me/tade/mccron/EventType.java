package me.tade.mccron;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EventType {

    JOIN_EVENT("join-event"),
    QUIT_EVENT("quit-event");

    @Getter
    private final String configName;

    public static EventType isEventJob(String string) {
        for (EventType type : values()) {
            if (type.getConfigName().equalsIgnoreCase(string))
                return type;
        }
        return null;
    }
}