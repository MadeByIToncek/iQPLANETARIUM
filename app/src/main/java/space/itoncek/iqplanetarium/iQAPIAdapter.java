/*
 * All rights reserved to IToncek
 */

package space.itoncek.iqplanetarium;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class iQAPIAdapter {
    private final Context context;

    public iQAPIAdapter(Context context) {
        this.context = context;
    }

    public List<Show> getDayShows(LocalDate day) throws JSONException, IOException, InterruptedException {
        String format = day.format(DateTimeFormatter.BASIC_ISO_DATE);
        String datest = format.substring(0, 4) + "-" + format.substring(4, 6) + "-" + format.substring(6, 8);
        AtomicReference<String> atomstring = new AtomicReference<>();
        System.out.println("Starting thread");
        Thread t = new Thread(()-> {
            URL url;
            try {
                System.out.println("Preparing URL");
                url = new URL("https://iqlandia.cz/ajax/_mod:PlanetariumReservations/_handler:PlanetariumReservationsAjax/case:getEvents/date:" + datest);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            StringBuilder sb = new StringBuilder();

            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                Scanner sc;
                if (conn.getResponseCode() == 200) {
                    sc = new Scanner(conn.getInputStream());
                } else {
                    sc = new Scanner(conn.getErrorStream());
                }
                System.out.println("Reciving");
                while (sc.hasNext()) {
                    sb.append(sc.next()).append(" ");
                }
                sc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(sb);
            atomstring.set(sb.toString());
        });

        t.start();
        t.join();

        System.out.println("Done, serializing");
        return serializeDay(atomstring.get());
    }

    public Seats getSeatsForShow(String id) throws InterruptedException, JSONException {
        AtomicReference<String> atomstring = new AtomicReference<>();
        System.out.println("Starting thread");
        Thread t = new Thread(() -> {
            URL url;
            try {
                System.out.println("Preparing URL");
                url = new URL("https://iqlandia.cz/ajax/_mod:PlanetariumReservations/_handler:PlanetariumReservationsAjax/case:getEventReservations/eventId:" + id);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            StringBuilder sb = new StringBuilder();
            try (Scanner sc = new Scanner(url.openStream())) {
                System.out.println("Reciving");
                while (sc.hasNext()) {
                    sb.append(sc.next()).append(" ");
                }
            } catch (IOException e) {
                System.out.println("Not recived...");
            }
            System.out.println(sb);
            atomstring.set(sb.toString());
        });

        t.start();
        t.join();
        return new Seats(atomstring.get());
    }

    private List<Show> serializeDay(String resp) throws JSONException {
        try {
            JSONObject obj = new JSONObject(resp);
            String error = obj.getString("error");
            Show s = new Show(LocalDateTime.now(), LocalDateTime.now().plus(1, ChronoUnit.SECONDS), error,"-1", 0);
            return List.of(s);
        } catch (JSONException e) {
            JSONArray arr = new JSONArray(resp);
            System.out.println(arr.toString(4));
            List<Show> shows = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                LocalDateTime start = LocalDateTime.parse(obj.getString("start"), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                LocalDateTime end = LocalDateTime.parse(obj.getString("end"), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                JSONObject event = obj.getJSONObject("event");
                String label = event.getString("label");
                String id = event.getString("checkoutSystemUniqueIdentifier");
                int seats = event.getInt("seatsAvailable");
                shows.add(new Show(start, end, label, id, seats));
            }
            return shows;
        }
    }


}

final class Show {
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String label;
    private final String id;
    private final int avaliable;

    Show(LocalDateTime start, LocalDateTime end, String label, String id, int avaliable) {
        this.start = start;
        this.end = end;
        this.label = label;
        this.id = id;
        this.avaliable = avaliable;
    }

    public LocalDateTime start() {
        return start;
    }

    public LocalDateTime end() {
        return end;
    }

    public String label() {
        return label;
    }

    public String id() {
        return id;
    }

    public int avaliable() {
        return avaliable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Show) obj;
        return Objects.equals(this.start, that.start) &&
                Objects.equals(this.end, that.end) &&
                Objects.equals(this.label, that.label) &&
                Objects.equals(this.id, that.id) &&
                this.avaliable == that.avaliable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, label, id, avaliable);
    }

    @NonNull
    @Override
    public String toString() {
        return "Show[" +
                "start=" + start + ", " +
                "end=" + end + ", " +
                "label=" + label + ", " +
                "id=" + id + ", " +
                "avaliable=" + avaliable + ']';
    }

}

class Seats {
    public HashMap<Seat, Boolean> seats = new HashMap<>();

    public Seats(String response) throws JSONException {
        JSONObject object = new JSONObject(response);
        for (int r = 1; r <= 5; r++) {
            JSONObject row = object.getJSONObject(String.valueOf(r));
            for (int s = 1; s <= seatAmt(r); s++) {
                seats.put(new Seat(r, s), row.getBoolean(String.valueOf(s)));
            }
        }
    }

    public int seatAmt(int row) {
        return switch (row) {
            case 1 -> 8;
            case 2 -> 13;
            case 3 -> 12;
            case 4 -> 11;
            case 5 -> 6;
            default -> throw new IllegalStateException("Unexpected value: " + row);
        };
    }
}

final class Seat {
    private final int row;
    private final int seat;

    Seat(int row, int seat) {
        this.row = row;
        this.seat = seat;
    }

    public int row() {
        return row;
    }

    public int seat() {
        return seat;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Seat) obj;
        return this.row == that.row &&
                this.seat == that.seat;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, seat);
    }

    @NonNull
    @Override
    public String toString() {
        return "Seat[" +
                "row=" + row + ", " +
                "seat=" + seat + ']';
    }

}
