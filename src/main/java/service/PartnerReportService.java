package service;

import dao.IPartnerReportDao;
import dao.PartnerReportDao;
import model.report.PartnerReportSummary;
import model.report.PartnerBikeRevenueItem;
import model.report.PartnerStoreRevenueItem;

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

public class PartnerReportService implements IPartnerReportService {
    private final IPartnerReportDao dao = new PartnerReportDao();
    private static final BigDecimal PARTNER_SHARE = new BigDecimal("0.40");

    @Override
    public PartnerReportSummary getSummary(int partnerId, Date from, Date to) throws Exception {
        return dao.getSummaryByPartner(partnerId, from, to);
    }

    @Override
    public List<PartnerBikeRevenueItem> getBikeRevenue(int partnerId, Date from, Date to) throws Exception {
        return dao.getBikeRevenueByPartner(partnerId, from, to);
    }

    @Override
    public List<PartnerStoreRevenueItem> getStoreRevenue(int partnerId, Date from, Date to) throws Exception {
        return dao.getStoreRevenueByPartner(partnerId, from, to, PARTNER_SHARE);
    }
}
