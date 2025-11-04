package model.adminfeedback;

import java.math.BigDecimal;

public class TopRow {
    private int id;
    private String name;
    private BigDecimal avgRating;
    private int count;

    public TopRow() {}
    public TopRow(int id, String name, BigDecimal avgRating, int count) {
        this.id = id; this.name = name; this.avgRating = avgRating; this.count = count;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAvgRating() { return avgRating; }
    public void setAvgRating(BigDecimal avgRating) { this.avgRating = avgRating; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
