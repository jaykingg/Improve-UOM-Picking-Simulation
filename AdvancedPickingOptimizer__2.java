import java.util.*;
import java.util.stream.Collectors;

public class AdvancedPickingOptimizer__2 {
    private static final List<UnitOfMeasure> unitOfMeasurement = Arrays.asList(UnitOfMeasure.CS, UnitOfMeasure.PK, UnitOfMeasure.EA);

    public static void main(String[] args) {
        System.out.println("***************************************************************************");
        //printCase(Arrays.asList(1, 3, 10, 11, 23, 50,100,100000),new ArrayList<>(Database.getItems("planning_1")));
        System.out.println("***************************************************************************");
        printCase(Arrays.asList(22), Database.getItems("case2"));
        System.out.println("***************************************************************************");
        //printCase(Arrays.asList(1, 3, 10, 11, 23, 100), Database.getItems("local"));
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
            System.out.println("---------------------------------------------");
        });
    }

    private static List<Item> copyItemList(List<Item> itemList) {
        return itemList.stream()
                .map(item -> new Item(item.itemId, item.stockId, item.usableStockEaQuantity, item.unitOfMeasurement.name(), item.unitOfMeasurementValue))
                .collect(Collectors.toList());
    }

    public static PickingResult optimizePicking(int orderQuantity, List<Item> items) {
        Map<UnitOfMeasure, Integer> resultForPrint = new HashMap<>(Map.of(UnitOfMeasure.CS, 0, UnitOfMeasure.PK, 0, UnitOfMeasure.EA, 0));
        Map<String, List<Item>> pickedItems = new HashMap<>();
        int orderEaQuantity = orderQuantity;

        for (UnitOfMeasure uom : unitOfMeasurement) {
            List<Item> filteredItems = items.stream()
                    .filter(item -> item.unitOfMeasurement.equals(uom))
                    .sorted(Comparator.comparingInt(item -> -item.usableStockEaQuantity))
                    .toList();

            for (Item item : filteredItems) {
                if (orderEaQuantity <= 0) break;
                if (item.usableStockEaQuantity < orderEaQuantity) continue;
                int simulatedOrderUomQuantity = Math.min(orderEaQuantity / item.unitOfMeasurementValue, item.usableStockUomQuantity);
                resultForPrint.put(uom, resultForPrint.get(uom) + simulatedOrderUomQuantity);
                orderEaQuantity -= simulatedOrderUomQuantity * item.unitOfMeasurementValue;


                item.usableStockUomQuantity -= simulatedOrderUomQuantity;
                item.usableStockEaQuantity = item.usableStockUomQuantity * item.unitOfMeasurementValue;
                item.pickedStockUomQuantity += simulatedOrderUomQuantity;
                item.pickedStockEaQuantity = item.pickedStockUomQuantity * item.unitOfMeasurementValue;

                if (simulatedOrderUomQuantity > 0) {
                    pickedItems.computeIfAbsent(uom.name(), k -> new ArrayList<>())
                            .stream()
                            .filter(pickedItem -> pickedItem.stockId == item.stockId)
                            .findFirst()
                            .ifPresentOrElse(
                                    pickedItem -> {
                                        pickedItem.usableStockUomQuantity -= simulatedOrderUomQuantity;
                                        pickedItem.usableStockEaQuantity = pickedItem.usableStockUomQuantity * pickedItem.unitOfMeasurementValue;
                                        pickedItem.pickedStockUomQuantity += simulatedOrderUomQuantity;
                                        pickedItem.pickedStockEaQuantity = pickedItem.pickedStockUomQuantity * pickedItem.unitOfMeasurementValue;
                                    },
                                    () -> {
                                        pickedItems.get(uom.name()).add(item);
                                    }
                            );
                }
            }
        }

        if (orderEaQuantity > 0) {
            List<UnitOfMeasure> unitOfMeasurementReverse = new ArrayList<>(unitOfMeasurement);
            Collections.reverse(unitOfMeasurementReverse);
            for (UnitOfMeasure uom : unitOfMeasurementReverse) {
                List<Item> filteredItems = items.stream()
                        .filter(item -> item.unitOfMeasurement.equals(uom))
                        .sorted(Comparator.comparingInt(item -> -item.usableStockEaQuantity))
                        .toList();

                for (Item item : filteredItems) {
                    if (orderEaQuantity <= 0) break;
                    if (item.usableStockUomQuantity > 0 && item.usableStockEaQuantity >= orderEaQuantity) {
                        resultForPrint.put(uom, resultForPrint.get(uom) + 1);
                        orderEaQuantity = item.unitOfMeasurementValue - orderEaQuantity;

                        item.usableStockUomQuantity -= 1;
                        item.usableStockEaQuantity = item.usableStockUomQuantity * item.unitOfMeasurementValue;
                        item.pickedStockUomQuantity += 1;
                        item.pickedStockEaQuantity = item.pickedStockUomQuantity * item.unitOfMeasurementValue;
                        item.remainStockEaQuantity = orderEaQuantity;

                        System.out.printf("재적재 재고 UOM/수량:: [uom: %s] [orderEaQuantity(EA): %d]%n", uom, orderEaQuantity);

                        pickedItems.computeIfAbsent(uom.name(), k -> new ArrayList<>())
                                .stream()
                                .filter(pickedItem -> pickedItem.stockId == item.stockId)
                                .findFirst()
                                .ifPresentOrElse(
                                        pickedItem -> {
                                            pickedItem.usableStockUomQuantity -= 1;
                                            pickedItem.usableStockEaQuantity = pickedItem.usableStockUomQuantity * pickedItem.unitOfMeasurementValue;
                                            pickedItem.pickedStockUomQuantity += 1;
                                            pickedItem.pickedStockEaQuantity = pickedItem.pickedStockUomQuantity * pickedItem.unitOfMeasurementValue;
                                        },
                                        () -> {
                                            pickedItems.get(uom.name()).add(item);
                                        }
                                );
                        return new PickingResult(resultForPrint, pickedItems);
                    }
                }
            }
            System.out.println("주문 수량을 충족시킬 수 없습니다.");
            return new PickingResult(new HashMap<>(), new HashMap<>());
        } else {
            return new PickingResult(resultForPrint, pickedItems);
        }
    }
}

class PickingResult {
    private final Map<UnitOfMeasure, Integer> result;
    private final Map<String, List<Item>> pickedItems;

    public PickingResult(Map<UnitOfMeasure, Integer> result, Map<String, List<Item>> pickedItems) {
        this.result = result;
        this.pickedItems = pickedItems;
    }

    public Map<UnitOfMeasure, Integer> getResult() {
        return result;
    }

    public List<Item> getItemsByUom(String uom) {
        return pickedItems.getOrDefault(uom, Collections.emptyList());
    }

    public boolean isEmpty() {
        return result.values().stream().allMatch(count -> count == 0);
    }
}
