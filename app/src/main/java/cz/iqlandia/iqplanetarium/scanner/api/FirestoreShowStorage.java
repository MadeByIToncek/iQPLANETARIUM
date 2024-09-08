package cz.iqlandia.iqplanetarium.scanner.api;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class FirestoreShowStorage {
    private final FirebaseFirestore db;

    public FirestoreShowStorage() {
        db = FirebaseFirestore.getInstance();
    }

    public void saveShow(DayShowsInfo.Event show) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", show.name);
        map.put("date", show.start.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        map.put("occupied", show.CurrentCapacity);

        db.collection("shows").document(show.reservationItemID + "").set(map, SetOptions.merge());
    }
}
