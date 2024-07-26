import lombok.Getter;
import lombok.Setter;

public class Item {
    int itemId;
    int stockId;
    int usableStockEaQuantity;
    UnitOfMeasure unitOfMeasurement;
    int unitOfMeasurementValue;
    int remainStockEaQuantity;
    int pickedStockUomQuantity;
    int pickedStockEaQuantity;

    public Item(int itemId, int stockId, int usableStockQuantity, String unitOfMeasurement, int uomConvertToEaValue) {
        this.itemId = itemId;
        this.stockId = stockId;
        this.usableStockEaQuantity = usableStockQuantity;
        this.unitOfMeasurement = UnitOfMeasure.valueOf(unitOfMeasurement);
        this.unitOfMeasurementValue = uomConvertToEaValue;
        this.remainStockEaQuantity = 0;
        this.pickedStockUomQuantity = 0;
        this.pickedStockEaQuantity = 0;
    }

    @Override
    public String toString() {
        return String.format("[ItemID: %d, StockID: %d, usableStockEaQuantity: %d, pickedStockUomQuantity: %d, pickedStockEaQuantity: %d, unitOfMeasurement: %s, UOM ConvertToEA: %d, remainStockEaQuantity: %d]",
                itemId, stockId, usableStockEaQuantity, pickedStockUomQuantity, pickedStockEaQuantity, unitOfMeasurement, unitOfMeasurementValue, remainStockEaQuantity);
    }
}
