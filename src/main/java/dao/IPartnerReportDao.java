package dao;

import model.report.PartnerReportSummary;
import model.report.PartnerBikeRevenueItem;
import model.report.PartnerStoreRevenueItem;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface IPartnerReportDao {
    PartnerReportSummary getSummaryByPartner(int partnerId, Date from, Date to) throws Exception;
    List<PartnerBikeRevenueItem> getBikeRevenueByPartner(int partnerId, Date from, Date to) throws Exception;
    List<PartnerStoreRevenueItem> getStoreRevenueByPartner(int partnerId, Date from, Date to, BigDecimal shareRate) throws Exception;
}
