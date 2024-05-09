package cz.iqlandia.iqplanetarium.api;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class DayShowsInfo {
    @NonNull
    public Status status;
    @NonNull
    public TreeSet<Event> events;

    public DayShowsInfo(@NonNull Status status, @NonNull TreeSet<Event> events) {
        this.status = status;
        this.events = events;
    }
    public enum Status {
        SUCCESS("success"),
        ERROR("error"),
        UNKNOWN("unknown");

        public final String status;
        Status(String status) {
            this.status = status;
        }

        @NonNull
        public static Status getStatus(@NonNull String status) {
            for (Status value : Status.values()) {
                if(value.status.equals(status)) {
                    return value;
                }
            }
            return UNKNOWN;
        }
    }

    public static class Event implements Serializable, Comparable<Event> {
        @NonNull
        public ArrayList<Prices> prices;
        @NonNull
        public ZonedDateTime start;
        @NonNull
        public ZonedDateTime end;
        @NonNull
        public String name;
        public Integer reservationID;
        public Integer reservationItemID;
        public Integer CurrentCapacity;
        public Integer MaxCapacity;
        @NonNull
        public TreeMap<Integer, TreeMap<Integer, SeatState>> seatmap;

        public Event(@NonNull ArrayList<Prices> prices,
                     @NonNull ZonedDateTime start,
                     @NonNull ZonedDateTime end,
                     @NonNull String name,
                     int reservationID,
                     int reservationItemID,
                     int currentCapacity,
                     int maxCapacity,
                     @NonNull TreeMap<Integer, TreeMap<Integer, SeatState>> seatmap) {
            this.prices = prices;
            this.start = start;
            this.end = end;
            this.name = name;
            this.reservationID = reservationID;
            this.reservationItemID = reservationItemID;
            this.CurrentCapacity = currentCapacity;
            this.MaxCapacity = maxCapacity;
            this.seatmap = seatmap;

        }

        @Override
        public int compareTo(Event o) {
            if(o.start.isBefore(this.start)) {
                return 1;
            } else if(o.start.equals(this.start)) {
                return 0;
            } else {
                return -1;
            }
        }


        public static class Prices implements Serializable{
            public int ID;
            @NonNull
            public String name;
            public int price;
            public Prices(int ID, @NonNull String name, int price) {
                this.ID = ID;
                this.name = name;
                this.price = price;
            }
        }

        public static enum SeatState implements Serializable {
            EMTPY,
            OCCUPIED,
            ENTRY_ALLOWED,
            POSSIBLE_DUPLICATE
        }
    }
}
