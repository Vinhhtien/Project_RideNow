package model;


public class BikeType {
    private int typeId;
    private String typeName; // 'Xe số', 'Xe ga', 'Phân khối lớn'


    public BikeType() {
    }

    public BikeType(int typeId, String typeName) {
        this.typeId = typeId;
        this.typeName = typeName;
    }


    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}