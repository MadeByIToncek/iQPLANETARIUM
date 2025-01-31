package cz.iqlandia.iqplanetarium.scanner.api;

import java.time.ZonedDateTime;

public class OpeningHours {
    public final ZonedDateTime start;
    public final ZonedDateTime end;

    public OpeningHours(ZonedDateTime start, ZonedDateTime end) {
        this.start = start;
        this.end = end;
    }

    public boolean isOpen(ZonedDateTime now) {
        return now.isAfter(start) && now.isBefore(end);
    }
}
