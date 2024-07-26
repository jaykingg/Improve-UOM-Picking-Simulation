import java.util.ArrayList;
import java.util.List;

public class Database {
    public static List<Item> getItems(String testcase) {
        List<Item> items = new ArrayList<>();
        switch (testcase) {
            case "dev":
                items.add(new Item(0, 1010, 890, "CS", 10));
                items.add(new Item(0, 1011, 15, "PK", 5));
                items.add(new Item(0, 1012, 893, "EA", 1));
                items.add(new Item(0, 1013, 117, "EA", 1));
                items.add(new Item(0, 1014, 20, "EA", 1));
                items.add(new Item(0, 1015, 2, "EA", 1));
                items.add(new Item(0, 1016, 1, "EA", 1));
                break;
            case "planning_1":
                items.add(new Item(2, 1020, 240, "CS", 24));
                items.add(new Item(2, 1021, 60, "PK", 6));
                items.add(new Item(2, 1021, 10, "EA", 1));
                break;
            case "planning_2":
                items.add(new Item(2, 1030, 240, "CS", 24));
                items.add(new Item(2, 1031, 60, "PK", 6));
                items.add(new Item(2, 1032, 0, "EA", 1));
                break;
            case "local":
                items.add(new Item(3, 1040, 20, "CS", 20));
                items.add(new Item(3, 1041, 5, "PK", 5));
                items.add(new Item(3, 1042, 5, "EA", 1));
                break;
        }

        return items;
    }
}
