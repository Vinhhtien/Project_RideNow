package service;

import model.report.PartnerReportSummary;
import model.report.PartnerBikeRevenueItem;
import model.report.PartnerStoreRevenueItem;

import java.util.Date;
import java.util.List;

public interface IPartnerReportService {
    PartnerReportSummary getSummary(int partnerId, Date from, Date to) throws Exception;
    List<PartnerBikeRevenueItem> getBikeRevenue(int partnerId, Date from, Date to) throws Exception;
    List<PartnerStoreRevenueItem> getStoreRevenue(int partnerId, Date from, Date to) throws Exception;
}
