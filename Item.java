public class Item {
    int itemId;
    int stockId;
    int usableStockUomQuantity;
    int usableStockEaQuantity;
    UnitOfMeasure unitOfMeasurement;
    int unitOfMeasurementValue;
    int remainStockEaQuantity;
    int pickedStockUomQuantity;
    int pickedStockEaQuantity;

    public Item(int itemId, int stockId, int usableStockEaQuantity, String unitOfMeasurement, int uomConvertToEaValue) {
        this.itemId = itemId;
        this.stockId = stockId;
        this.usableStockUomQuantity = usableStockEaQuantity / uomConvertToEaValue;
        this.usableStockEaQuantity = usableStockEaQuantity;
        this.unitOfMeasurement = UnitOfMeasure.valueOf(unitOfMeasurement);
        this.unitOfMeasurementValue = uomConvertToEaValue;
        this.remainStockEaQuantity = 0;
        this.pickedStockUomQuantity = 0;
        this.pickedStockEaQuantity = 0;
    }

    @Override
    public String toString() {
        return String.format("[ItemID: %d, StockID: %d, usableStockUomQuantity: %d, usableStockEaQuantity: %d, pickedStockUomQuantity: %d, pickedStockEaQuantity: %d, unitOfMeasurement: %s, UOM ConvertToEA: %d, remainStockEaQuantity: %d]",
                itemId, stockId,usableStockUomQuantity, usableStockEaQuantity, pickedStockUomQuantity, pickedStockEaQuantity, unitOfMeasurement, unitOfMeasurementValue, remainStockEaQuantity);
    }
}
