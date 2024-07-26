import java.util.*;
import java.util.stream.Collectors;

public class AdvancedPickingOptimizer__2 {
    private static final List<UnitOfMeasure> unitOfMeasurement = Arrays.asList(UnitOfMeasure.CS, UnitOfMeasure.PK, UnitOfMeasure.EA);

    public static void main(String[] args) {
        printCase(Arrays.asList(1, 3, 10, 11, 23, 50, 100000), Database.getItems("local"));
        System.out.println("***************************************************************************");
    }

    public static void printCase(List<Integer> orderList, List<Item> itemList) {
        orderList.forEach(order -> {
            List<Item> copiedItemList = copyItemList(itemList);
            PickingResult pickingResult = optimizePicking(order, copiedItemList);
            System.out.printf("주문 수량:: [%d]%n", order);
            if (!pickingResult.isEmpty()) {
                pickingResult.getResult().forEach((uom, count) -> {
                    if (count > 0) {
                        System.out.println(uom + ": " + count + "개");
                        pickingResult.getItemsByUom(uom.name()).forEach(System.out::println);
                    }
                });
            }
            System.out.println("---------------------------------------------");
        });
    }

    private static List<Item> copyItemList(List<Item> itemList) {
        return itemList.stream()
                .map(item -> new Item(item.itemId, item.stockId, item.usableStockEaQuantity, item.unitOfMeasurement.name(), item.unitOfMeasurementValue))
                .collect(Collectors.toList());
    }

    public static PickingResult optimizePicking(int orderQuantity, List<Item> items) {
        Map<UnitOfMeasure, Integer> result = new HashMap<>(Map.of(UnitOfMeasure.CS, 0, UnitOfMeasure.PK, 0, UnitOfMeasure.EA, 0));
        Map<String, List<Item>> pickedItems = new HashMap<>();
        int remainingStockQuantity = orderQuantity;

        for (UnitOfMeasure uom : unitOfMeasurement) {
            List<Item> filteredItems = items.stream()
                    .filter(item -> item.unitOfMeasurement.equals(uom))
                    .sorted(Comparator.comparingInt(item -> -item.unitOfMeasurementValue))
                    .toList();

            for (Item item : filteredItems) {
                if (remainingStockQuantity == 0) break;
                int processedUomStockQuantity = Math.min(remainingStockQuantity / item.unitOfMeasurementValue, item.usableStockEaQuantity);
                result.put(uom, result.get(uom) + processedUomStockQuantity);
                remainingStockQuantity -= processedUomStockQuantity * item.unitOfMeasurementValue;
                item.usableStockEaQuantity -= processedUomStockQuantity;

                if (processedUomStockQuantity > 0) {
                    List<Item> itemsByUom = pickedItems.computeIfAbsent(uom.name(), k -> new ArrayList<>());
                    Optional<Item> existingItem = itemsByUom.stream()
                            .filter(pickedItem -> pickedItem.stockId == item.stockId)
                            .findFirst();

                    if (existingItem.isPresent()) {
                        existingItem.get().usableStockEaQuantity += processedUomStockQuantity;
                    } else {
                        Item newItem = new Item(item.itemId, item.stockId, processedUomStockQuantity, item.unitOfMeasurement.name(), item.unitOfMeasurementValue);
                        itemsByUom.add(newItem);
                    }
                }
            }
        }

        if (remainingStockQuantity > 0) {
            List<UnitOfMeasure> unitOfMeasurementReverse = new ArrayList<>(unitOfMeasurement);
            Collections.reverse(unitOfMeasurementReverse);
            for (UnitOfMeasure uom : unitOfMeasurementReverse) {
                List<Item> filteredItems = items.stream()
                        .filter(item -> item.unitOfMeasurement.equals(uom))
                        .sorted(Comparator.comparingInt(item -> -item.unitOfMeasurementValue))
                        .toList();

                for (Item item : filteredItems) {
                    if (remainingStockQuantity == 0) break;
                    if (item.usableStockEaQuantity > 0) {
                        result.put(uom, result.get(uom) + 1);
                        remainingStockQuantity = item.unitOfMeasurementValue - remainingStockQuantity;

                        System.out.printf("재적재 재고 UOM/수량:: [uom: %s] [remainingStockQuantity(EA): %d]%n", uom, remainingStockQuantity);

                        List<Item> itemsByUom = pickedItems.computeIfAbsent(uom.name(), k -> new ArrayList<>());
                        Optional<Item> existingItem = itemsByUom.stream()
                                .filter(pickedItem -> pickedItem.stockId == item.stockId)
                                .findFirst();

                        if (existingItem.isPresent()) {
                            existingItem.get().usableStockEaQuantity += 1;
                        } else {
                            Item newItem = new Item(item.itemId, item.stockId, 1, item.unitOfMeasurement.name(), item.unitOfMeasurementValue);
                            itemsByUom.add(newItem);
                        }
                        return new PickingResult(result, pickedItems);
                    }
                }
            }
            System.out.println("주문 수량을 충족시킬 수 없습니다.");
            return new PickingResult(new HashMap<>(), new HashMap<>());
        } else {
            return new PickingResult(result, pickedItems);
        }
    }
}

class PickingResult__2 {
    private final Map<UnitOfMeasure, Integer> result;
    private final Map<String, List<Item>> pickedItems;

    public PickingResult__2(Map<UnitOfMeasure, Integer> result, Map<String, List<Item>> pickedItems) {
        this.result = result;
        this.pickedItems = pickedItems;
    }

    public Map<UnitOfMeasure, Integer> getResult() {
        return result;
    }

    public List<Item> getItemsByUom(String uom) {
        return pickedItems.getOrDefault(UnitOfMeasure.valueOf(uom), Collections.emptyList());
    }

    public boolean isEmpty() {
        return result.values().stream().allMatch(count -> count == 0);
    }
}
