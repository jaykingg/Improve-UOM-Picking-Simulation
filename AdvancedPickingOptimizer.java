import java.util.*;
import java.util.stream.Collectors;

public class AdvancedPickingOptimizer {
    private static final List<UnitOfMeasure> unitOfMeasurement = Arrays.asList(UnitOfMeasure.CS,UnitOfMeasure.PK, UnitOfMeasure.EA);

    public static void main(String[] args) {
        //printCase(Arrays.asList(1, 3, 10, 11, 23, 50,100,100000),new ArrayList<>(Database.getItems("planning_1")));
        System.out.println("***************************************************************************");
        //printCase(Arrays.asList(1, 3, 10, 11, 23, 50,100,100000), Database.getItems("planning_2"));
        System.out.println("***************************************************************************");
        printCase(Arrays.asList(11),Database.getItems("local"));
        System.out.println("***************************************************************************");
        //printCase(Arrays.asList(1, 3, 10, 11, 23, 50,100000),Database.getItems("dev"));
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
            pickingResult.getResult().clear();
            System.out.println("---------------------------------------------");
        });
    }

    private static List<Item> copyItemList(List<Item> itemList) {
        return itemList.stream()
                .map(item -> new Item(item.itemId, item.stockId, item.usableStockEaQuantity, item.unitOfMeasurement.name(), item.unitOfMeasurementValue))
                .collect(Collectors.toList());
    }

    public static PickingResult optimizePicking(int inputOrderEaQuantity, List<Item> items) {
        Map<UnitOfMeasure, Integer> resultOfSelectedUomQuantity = new HashMap<>(Map.of(UnitOfMeasure.CS, 0, UnitOfMeasure.PK, 0, UnitOfMeasure.EA, 0));
        Map<String, List<Item>> pickedItems = new HashMap<>();
        List<Item> filteredItems;
        // 11
        int orderEaQuantity = inputOrderEaQuantity;

        // CS - PK - EA
        for (UnitOfMeasure uom : unitOfMeasurement) {
            filteredItems = items.stream()
                    .filter(item -> item.unitOfMeasurement.equals(uom))
                    .sorted(Comparator.comparingInt(item -> -item.usableStockEaQuantity))
                    .toList();

            System.out.println("FUCK::"+orderEaQuantity);
            System.out.println(filteredItems);
            for (Item item : filteredItems) {
                if (orderEaQuantity <= 0) break;
                int simulatedPickedUomStockQuantity = Math.min(orderEaQuantity / item.unitOfMeasurementValue, item.usableStockEaQuantity);
                System.out.println(simulatedPickedUomStockQuantity);

                /* console 개수 확인용 */
                resultOfSelectedUomQuantity.put(uom, resultOfSelectedUomQuantity.get(uom) + simulatedPickedUomStockQuantity);

                orderEaQuantity -= simulatedPickedUomStockQuantity * item.unitOfMeasurementValue;
                item.usableStockEaQuantity -= orderEaQuantity;
                item.pickedStockUomQuantity += simulatedPickedUomStockQuantity;
                item.pickedStockEaQuantity = item.pickedStockUomQuantity * item.unitOfMeasurementValue;

                if(simulatedPickedUomStockQuantity > 0) {


                    List<Item> itemsByUom = pickedItems.computeIfAbsent(uom.name(), k -> new ArrayList<>());
                    Optional<Item> existedItem = itemsByUom
                            .stream()
                            .filter(pickedItem -> pickedItem.stockId == item.stockId)
                            .findFirst();

                    if (existedItem.isPresent()) {
                        existedItem.get().usableStockEaQuantity += simulatedPickedUomStockQuantity;
                        existedItem.get().pickedStockUomQuantity += simulatedPickedUomStockQuantity;
                        existedItem.get().pickedStockEaQuantity = item.pickedStockUomQuantity * item.unitOfMeasurementValue;
                    } else {
                        Item newItem = new Item(item.itemId, item.stockId, simulatedPickedUomStockQuantity, item.unitOfMeasurement.name(), item.unitOfMeasurementValue);
                        itemsByUom.add(newItem);
                    }
                }
            }
        }


        if (orderEaQuantity > 0) {
            List<UnitOfMeasure> unitOfMeasurementReverse = unitOfMeasurement.reversed();
            for (UnitOfMeasure uom : unitOfMeasurementReverse) {
                filteredItems = items.stream()
                        .filter(item -> item.unitOfMeasurement.equals(uom))
                        .sorted(Comparator.comparingInt(item -> -item.usableStockEaQuantity))
                        .toList();

                for (Item item : filteredItems) {
                    if (item.usableStockEaQuantity > 0) {
                        resultOfSelectedUomQuantity.put(uom, resultOfSelectedUomQuantity.get(uom) + 1);
                        orderEaQuantity = item.unitOfMeasurementValue - orderEaQuantity;
                        item.remainStockEaQuantity = orderEaQuantity;
                        item.usableStockEaQuantity -= 1;
                        item.pickedStockUomQuantity += 1;
                        item.pickedStockEaQuantity = item.pickedStockUomQuantity * item.unitOfMeasurementValue;


                        if(orderEaQuantity > 0) {
                            System.out.printf("재적재 재고 UOM/수량:: [uom: %s] [orderEaQuantity(EA): %d]%n", uom, orderEaQuantity);
                            List<Item> itemsByUom = pickedItems.computeIfAbsent(uom.name(), k -> new ArrayList<>());
                            Optional<Item> existedItem = itemsByUom
                                    .stream()
                                    .filter(pickedItem -> pickedItem.stockId == item.stockId)
                                    .findFirst();

                            if (existedItem.isEmpty()) {
                                itemsByUom.add(item);
                            }
                        }
                        return new PickingResult(resultOfSelectedUomQuantity, pickedItems);
                    }
                }
            }
            System.out.println("주문 수량을 충족시킬 수 없습니다.");
            return new PickingResult(new HashMap<>(), new HashMap<>());
        } else {
            return new PickingResult(resultOfSelectedUomQuantity, pickedItems);
        }
    }
}

class PickingResult {
    private final Map<UnitOfMeasure, Integer> resultOfSelectedUomQuantity;
    private final Map<String, List<Item>> pickedItems;

    public PickingResult(Map<UnitOfMeasure, Integer> resultOfSelectedUomQuantity, Map<String, List<Item>> pickedItems) {
        this.resultOfSelectedUomQuantity = resultOfSelectedUomQuantity;
        this.pickedItems = pickedItems;
    }

    public Map<UnitOfMeasure, Integer> getResult() {
        return resultOfSelectedUomQuantity;
    }

    public List<Item> getItemsByUom(String uom) {
        return pickedItems.getOrDefault(uom, Collections.emptyList());
    }

    public boolean isEmpty() {
        return resultOfSelectedUomQuantity.values().stream().allMatch(count -> count == 0);
    }
}
