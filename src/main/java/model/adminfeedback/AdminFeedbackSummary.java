package model.adminfeedback;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminFeedbackSummary {
    private int countOverall;
    private int countStore;
    private int countBike;

    private BigDecimal avgOverall;
    private BigDecimal avgStore;
    private BigDecimal avgBike;

    // histogram 1..5
    private int[] histStore = new int[6];
    private int[] histBike  = new int[6];

    private List<TopRow> topStores = new ArrayList<>();
    private List<TopRow> topBikes  = new ArrayList<>();

    private Date from;
    private Date to;

    public int getCountOverall() { return countOverall; }
    public void setCountOverall(int countOverall) { this.countOverall = countOverall; }
    public int getCountStore() { return countStore; }
    public void setCountStore(int countStore) { this.countStore = countStore; }
    public int getCountBike() { return countBike; }
    public void setCountBike(int countBike) { this.countBike = countBike; }

    public BigDecimal getAvgOverall() { return avgOverall; }
    public void setAvgOverall(BigDecimal avgOverall) { this.avgOverall = avgOverall; }
    public BigDecimal getAvgStore() { return avgStore; }
    public void setAvgStore(BigDecimal avgStore) { this.avgStore = avgStore; }
    public BigDecimal getAvgBike() { return avgBike; }
    public void setAvgBike(BigDecimal avgBike) { this.avgBike = avgBike; }

    public int[] getHistStore() { return histStore; }
    public void setHistStore(int[] histStore) { this.histStore = histStore; }
    public int[] getHistBike() { return histBike; }
    public void setHistBike(int[] histBike) { this.histBike = histBike; }

    public List<TopRow> getTopStores() { return topStores; }
    public void setTopStores(List<TopRow> topStores) { this.topStores = topStores; }
    public List<TopRow> getTopBikes() { return topBikes; }
    public void setTopBikes(List<TopRow> topBikes) { this.topBikes = topBikes; }

    public Date getFrom() { return from; }
    public void setFrom(Date from) { this.from = from; }
    public Date getTo() { return to; }
    public void setTo(Date to) { this.to = to; }
}
