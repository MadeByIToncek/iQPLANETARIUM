/*
 * All rights reserved to IToncek
 */

package space.itoncek.iqplanetarium;

import android.content.Context;
import okhttp3.OkHttpClient;
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
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class iQAPIAdapter {
    private final OkHttpClient client;
    private final Context context;

    public iQAPIAdapter(Context context) {
        this.context = context;
        client = new OkHttpClient();
    }

    public List<Show> getDayShows(LocalDate day) throws JSONException, IOException, InterruptedException {
        String format = day.format(DateTimeFormatter.BASIC_ISO_DATE);
        String datest = format.substring(0, 4) + "-" + format.substring(4, 6) + "-" + format.substring(6, 8);
        AtomicReference<String> atomstring = new AtomicReference<>();
        Thread t = new Thread(()-> {
            URL url;
            try {
                url = new URL("https://iqlandia.cz/ajax/_mod:PlanetariumReservations/_handler:PlanetariumReservationsAjax/case:getEvents/date:" + datest);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            StringBuilder sb = new StringBuilder();
            try (Scanner sc = new Scanner(url.openStream())) {
                while (sc.hasNext()){
                    sb.append(sc.next()).append(" ");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            atomstring.set(sb.toString());
        });

        t.start();
        t.join();
        return serializeDay(atomstring.get());
    }

    private List<Show> serializeDay(String resp) throws JSONException, InterruptedException {
        JSONArray arr = new JSONArray(resp);
        System.out.println(arr.toString(4));
        List<Show> shows = new ArrayList<>();
        for (int i = 0; i < arr.length() - 1; i++) {
            JSONObject obj = arr.getJSONObject(i);
            LocalDateTime start = LocalDateTime.parse(obj.getString("start"),DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            LocalDateTime end = LocalDateTime.parse(obj.getString("end"),DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            JSONObject event = obj.getJSONObject("event");
            String label = event.getString("label");
            String id = event.getString("checkoutSystemUniqueIdentifier");
            int seats = event.getInt("seatsAvailable");
            shows.add(new Show(start,end,label,id,seats));
        }
        return shows;
    }


}

class Show {
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String label;
    private final String id;
    private final int avaliable;

    public Show(LocalDateTime start, LocalDateTime end, String label, String id, int avaliable) {

        this.start = start;
        this.end = end;
        this.label = label;
        this.id = id;
        this.avaliable = avaliable;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public String getLabel() {
        return label;
    }

    public String getId() {
        return id;
    }

    public int getAvaliable() {
        return avaliable;
    }
}

