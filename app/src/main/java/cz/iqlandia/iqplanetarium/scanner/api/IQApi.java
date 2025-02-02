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
    private static final HashMap<LocalDate, CachedCalendar> calcache = new HashMap<>();
    private static final OkHttpClient client = new OkHttpClient();

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
        DayShowsInfo normal = getShowInfoForDate(date, false);
        DayShowsInfo special = getShowInfoForDate(date, true);

        if(special != null && normal != null) {
            normal.events.addAll(special.events);
        }

        return normal;
    }

    public static @Nullable DayShowsInfo getShowInfoForDate(@NotNull LocalDate date, boolean special) throws IOException {
        try {
//            Log.d("IQApi", "Getting show info for date: " + date.format(DateTimeFormatter.ISO_LOCAL_DATE));

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            jsonBody.put("type", special ? "special" : "normal");

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

    public static Calendar getCalendarForMonth(@NotNull LocalDate date) throws IOException, JSONException {
        JSONObject normal = getCalendar(date, false);
        JSONObject special = getCalendar(date, true);

        JSONArray normalArray = normal.getJSONObject("data").getJSONArray("calendar");
        JSONArray specialArray = special.getJSONObject("data").getJSONArray("calendar");

        HashMap<LocalDate, Boolean> result = new HashMap<>(normalArray.length());

        for (int i = 0; i < normalArray.length(); i++) {
            JSONObject o = normalArray.getJSONObject(i);

            LocalDate d = LocalDate.parse(o.getString("date"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            boolean hasEvent = o.getBoolean("has_event");

            //Log.d("normal", d.format(DateTimeFormatter.ISO_LOCAL_DATE) + " is " + hasEvent);

            result.put(d, hasEvent);
        }

        for (int i = 0; i < specialArray.length(); i++) {
            JSONObject o = specialArray.getJSONObject(i);

            LocalDate d = LocalDate.parse(o.getString("date"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            boolean hasEvent = o.getBoolean("has_event");

            //Log.d("special", d.format(DateTimeFormatter.ISO_LOCAL_DATE) + " is " + hasEvent);

            if(result.containsKey(d)) {
                result.put(d, Boolean.TRUE.equals(result.get(d)) || hasEvent);
            } else {
                result.put(d, hasEvent);
            }
        }

        return new Calendar(result);
    }

    static JSONObject getCalendar(LocalDate date, boolean special) throws JSONException, IOException {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("month", date.getMonthValue());
        jsonBody.put("year", String.valueOf(date.getYear()));
        jsonBody.put("type", special?"special":"normal");

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url("https://api.iqlandia.cz/events/calendar")
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();

        assert response.body() != null;
        JSONObject resp = new JSONObject(response.body().string());

        response.close();
        return resp;
    }

    public static Calendar getCalendarForMonthCached(LocalDate date) throws JSONException, IOException {
        LocalDate first = LocalDate.of(date.getYear(), date.getMonth(), 1);
        Log.d("getCalendarCached()", "First day " + first.format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (calcache.containsKey(first)) {
            CachedCalendar cc = calcache.get(first);
            if (cc == null) {
                Log.d("getCalendarCached()", "Cache dead!");
                calcache.remove(first);
                return getCalendarForMonthCached(date);
            } else if (cc.purgeAt.isBefore(ZonedDateTime.now())) {
                Log.d("getCalendarCached()", "Cache stale!");
                calcache.remove(first);
                return getCalendarForMonthCached(date);
            } else {
                Log.d("getCalendarCached()", "Cache hit!");
                return cc.calendar;
            }
        } else {
            Log.d("getCalendarCached()", "Cache miss!");
            Calendar c = getCalendarForMonth(first);
            calcache.put(first, new CachedCalendar(c, ZonedDateTime.now().plusMinutes(5)));
            return c;
        }
    }

    public static OpeningHours isIqlOpenRightNow() throws JSONException, IOException {
        ZonedDateTime dateTime = ZonedDateTime.now();
        return getOpeningHoursCached(dateTime);
    }

    private static OpeningHours getOpeningHoursCached(ZonedDateTime dateTime) throws IOException, JSONException {
        Request request = new Request.Builder()
                .url("https://iqlandia.cz/objednavka/get_events/82?start=" + dateTime.toEpochSecond() +"&end="+(dateTime.toEpochSecond()+1))
                .get()
                .build();

        Response response = client.newCall(request).execute();

        assert response.body() != null;
        JSONArray resp = new JSONArray(response.body().string());

        response.close();

        for (int i = 0; i < resp.length(); i++) {
            JSONObject o = resp.getJSONObject(i);
            if(o.getString("world").equals("iQLANDIA")) {
                return new OpeningHours(LocalDateTime.parse(o.getString("start"),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(ZoneId.of("Europe/Prague")).withZoneSameLocal(ZoneId.of("Europe/Prague")),
                        LocalDateTime.parse(o.getString("end"),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(ZoneId.of("Europe/Prague")).withZoneSameLocal(ZoneId.of("Europe/Prague")));
            }
        }
        return null;
    }

    public static class CachedDayShowsInfo {
        final @Nullable DayShowsInfo info;
        final ZonedDateTime purgeAt;

        public CachedDayShowsInfo(@Nullable DayShowsInfo info, ZonedDateTime purgeAt) {
            this.info = info;
            this.purgeAt = purgeAt;
        }
    }

    private static class CachedCalendar {
        final Calendar calendar;
        final ZonedDateTime purgeAt;

        public CachedCalendar(Calendar calendar, ZonedDateTime purgeAt) {
            this.calendar = calendar;
            this.purgeAt = purgeAt;
        }
    }
}
