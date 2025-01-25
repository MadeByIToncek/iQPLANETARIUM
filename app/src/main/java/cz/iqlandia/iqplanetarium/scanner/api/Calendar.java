package cz.iqlandia.iqplanetarium.scanner.api;

import java.time.LocalDate;
import java.util.HashMap;

public class Calendar {
    HashMap<LocalDate, Boolean> cal;

    public Calendar(HashMap<LocalDate, Boolean> cal) {
        this.cal = cal;
    }

    public boolean isOpenOnDay(LocalDate localDate) {
        return Boolean.TRUE.equals(cal.get(localDate));
    }
}
