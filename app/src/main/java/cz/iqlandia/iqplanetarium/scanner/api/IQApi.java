package cz.iqlandia.iqplanetarium.scanner.api;

import android.util.Log;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IQApi {
    private static final HashMap<LocalDate, CachedDayShowsInfo> cache = new HashMap<>();

    public static @Nullable DayShowsInfo getShowInfoForDateCached(@NotNull LocalDate date) throws IOException {
        if (cache.containsKey(date)) {
            if (Objects.requireNonNull(cache.get(date)).purgeAt.isAfter(ZonedDateTime.now())) {
                Log.i("ShowCache", "Cache hit");
                return Objects.requireNonNull(cache.get(date)).info;
            } else {
                Log.i("ShowCache", "Cache stale");
                cache.remove(date);
                DayShowsInfo info = getShowInfoForDate(date);
                cache.put(date, new CachedDayShowsInfo(info, ZonedDateTime.now().plusMinutes(5)));
                return info;
            }
        } else {
            Log.i("ShowCache", "Cache miss");
            DayShowsInfo info = getShowInfoForDate(date);
            cache.put(date, new CachedDayShowsInfo(info, ZonedDateTime.now().plusMinutes(5)));
            return info;
        }
    }

    public static @Nullable DayShowsInfo getShowInfoForDate(@NotNull LocalDate date) throws IOException {
        try {
//            Log.d("IQApi", "Getting show info for date: " + date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            OkHttpClient client = new OkHttpClient();

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            jsonBody.put("type", "normal");

            RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url("https://api.iqlandia.cz/events/day-detail")
                    .post(requestBody)
                    .build();

//            Log.d("IQApi", "Sending request: " + request.url());
            Response response = client.newCall(request).execute();

            assert response.body() != null;
//            Log.d("IQApi", "Got response: " + response.code());
//            Log.d("IQApi", "Deserializing response");
            JSONObject resp = new JSONObject(response.body().string());
            response.close();

//            Log.d("IQApi", "Parsing response");
            DayShowsInfo.Status status = DayShowsInfo.Status.getStatus(resp.getString("status"));
            TreeSet<DayShowsInfo.Event> events = new TreeSet<>();

//            Log.d("IQApi", "Status: " + status);
            if (status != DayShowsInfo.Status.UNKNOWN) {
//                Log.d("IQApi", "Parsing events");
                JSONArray eventsArray = resp.getJSONObject("data").getJSONArray("events");

                for (int i = 0; i < eventsArray.length(); i++) {
                    JSONObject event = eventsArray.getJSONObject(i);

//                    Log.d("IQApi", "Parsing event: " + event.getString("Name"));

//                    Log.d("IQApi", "Parsing prices");
                    // Parsing prices
                    ArrayList<DayShowsInfo.Event.Prices> prices = new ArrayList<>();
                    for (int j = 0; j < event.getJSONArray("Articles").length(); j++) {
                        JSONObject price = event.getJSONArray("Articles").getJSONObject(j);
                        prices.add(new DayShowsInfo.Event.Prices(price.getInt("ID"), price.getString("Name"), price.getInt("Price")));
                    }

//                    Log.d("IQApi", "Prices: " + prices);
//                    Log.d("IQApi", "Parsing start and end");
                    // Parsing start and end
                    ZonedDateTime start = LocalDateTime.parse(event.getString("DateFrom")).atZone(ZoneId.systemDefault());
                    ZonedDateTime end = LocalDateTime.parse(event.getString("DateTo")).atZone(ZoneId.of("Europe/Prague"));

//                    Log.d("IQApi", "Start: " + start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//                    Log.d("IQApi", "End: " + end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

//                    Log.d("IQApi", "Parsing meta");
                    //Parsing meta
                    String name = event.getString("Name");
                    int reservationID = event.getInt("ReservationID");
                    int reservationItemID = event.getInt("ReservationItemID");
                    int currentCapacity = event.getInt("SingleEntrances");
                    int maxCapacity = event.getInt("TotalCapacity");

//                    Log.d("IQApi", "Parsing seats");
                    //Parsing seats
                    TreeMap<Integer, TreeMap<Integer, DayShowsInfo.Event.SeatState>> seats = new TreeMap<>();

                    JSONArray arr = event.getJSONArray("Sectors").getJSONObject(0).getJSONArray("Rows");
                    for (int j = 0; j < arr.length(); j++) {
                        JSONObject row = arr.getJSONObject(j);
                        TreeMap<Integer, DayShowsInfo.Event.SeatState> columns = new TreeMap<>();

                        for (int k = 0; k < row.getJSONArray("Seats").length(); k++) {
                            JSONObject seat = row.getJSONArray("Seats").getJSONObject(k);
                            columns.put(Integer.parseInt(seat.getString("Number")), seat.getInt("Status") == 1 ? DayShowsInfo.Event.SeatState.OCCUPIED : DayShowsInfo.Event.SeatState.EMTPY);
                        }

                        seats.put(Integer.parseInt(row.getString("Number")), columns);
                    }

//                    Log.d("IQApi", "Finishing");
                    events.add(new DayShowsInfo.Event(prices,
                            start,
                            end,
                            name,
                            reservationID,
                            reservationItemID,
                            currentCapacity,
                            maxCapacity,
                            seats));

                }

                if (events.isEmpty()) return null;
                else return new DayShowsInfo(status, events);
            } else {
                return null;
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static class CachedDayShowsInfo {
        final @Nullable DayShowsInfo info;
        final ZonedDateTime purgeAt;

        public CachedDayShowsInfo(@Nullable DayShowsInfo info, ZonedDateTime purgeAt) {
            this.info = info;
            this.purgeAt = purgeAt;
        }
    }
}
