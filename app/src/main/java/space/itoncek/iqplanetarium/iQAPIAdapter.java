/*
 * All rights reserved to IToncek
 */

package space.itoncek.iqplanetarium;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            try (Scanner sc = new Scanner(url.openStream())) {
                System.out.println("Reciving");
                while (sc.hasNext()){
                    sb.append(sc.next()).append(" ");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            }
            System.out.println(sb);
            atomstring.set(sb.toString());
        });

        t.start();
        t.join();
        return new Seats(atomstring.get());
    }

    private List<Show> serializeDay(String resp) throws JSONException {
        JSONArray arr = new JSONArray(resp);
        System.out.println(arr.toString(4));
        List<Show> shows = new ArrayList<>();
        for (int i = 0; i < arr.length() - 1; i++) {
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

record Show(LocalDateTime start, LocalDateTime end, String label, String id, int avaliable) {
}

class Seats {
    public HashMap<Seat, Boolean> seats = new HashMap<>();

    public Seats(String response) throws JSONException {
        JSONObject object = new JSONObject(response);
        for (int r = 1; r <= 5; r++) {
            JSONObject row = object.getJSONObject(String.valueOf(r));
            for (int s = 1; s < seatAmt(r); s++) {
                seats.put(new Seat(r, s), row.getBoolean(String.valueOf(s)));
            }
        }
    }

    private int seatAmt(int row) {
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

record Seat(int row, int seat) {
}
