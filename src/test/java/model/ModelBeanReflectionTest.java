package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ModelBeanReflectionTest {

    private Object sampleFor(Class<?> t) {
        if (t == String.class) return "x";
        if (t == int.class || t == Integer.class) return 1;
        if (t == long.class || t == Long.class) return 2L;
        if (t == double.class || t == Double.class) return 1.5d;
        if (t == boolean.class || t == Boolean.class) return true;
        if (t == BigDecimal.class) return new BigDecimal("12.34");
        if (t == java.util.Date.class) return java.util.Date.from(Instant.ofEpochMilli(1234567890L));
        if (t == Timestamp.class) return new Timestamp(1234567890L);
        if (t == Date.class) return new Date(1234567890L);
        return null;
    }

    private void exerciseBean(Class<?> clazz) throws Exception {
        Constructor<?> ctor = clazz.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object bean = ctor.newInstance();

        Map<String, Method> getters = new HashMap<>();
        for (Method m : clazz.getMethods()) {
            if ((m.getName().startsWith("get") || m.getName().startsWith("is")) && m.getParameterCount() == 0) {
                getters.put(m.getName(), m);
            }
        }

        for (Method m : clazz.getMethods()) {
            if (m.getName().startsWith("set") && m.getParameterCount() == 1) {
                Class<?> pt = m.getParameterTypes()[0];
                Object sample = sampleFor(pt);
                if (sample == null) continue; // skip unsupported types
                m.invoke(bean, sample);

                String base = m.getName().substring(3);
                Method g = getters.get("get" + base);
                if (g == null) g = getters.get("is" + base);
                if (g != null) {
                    Object val = g.invoke(bean);
                    if (val instanceof BigDecimal && sample instanceof BigDecimal) {
                        assertThat(((BigDecimal) val).compareTo((BigDecimal) sample)).isZero();
                    } else {
                        assertThat(val).isEqualTo(sample);
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Exercise common bean getters/setters in model and subpackages")
    void exerciseAllBeans() throws Exception {
        Class<?>[] classes = new Class<?>[]{
                // model
                Account.class, BikeType.class, Customer.class, GoogleUser.class,
                Motorbike.class, MotorbikeListItem.class, Notification.class,
                OrderDetail.class, OrderDetailItem.class, OrderListItem.class,
                OrderStatusHistory.class, OrderSummary.class, OrderVm.class,
                Partner.class, PaymentInfo.class, RefundInfo.class, RentalOrder.class,
                Review.class, ReviewStats.class, ScheduleItem.class, StoreReview.class,
                // adminfeedback
                model.adminfeedback.AdminFeedbackItem.class,
                model.adminfeedback.AdminFeedbackSummary.class,
                // report
                model.report.AdminDailyRevenuePoint.class,
                model.report.AdminOrderDetail.class,
                model.report.AdminOutstandingOrderItem.class,
                model.report.AdminPaymentMethodStat.class,
                model.report.AdminRefundItem.class,
                model.report.AdminReportSummary.class,
                model.report.AdminRevenueItem.class,
                model.report.AdminTopCustomerStat.class,
                model.report.PartnerBikeRevenueItem.class,
                model.report.PartnerReportSummary.class,
                model.report.PartnerStoreRevenueItem.class
        };

        for (Class<?> c : classes) {
            try {
                exerciseBean(c);
            } catch (NoSuchMethodException ignore) {
                // skip classes without default constructor
            }
        }
    }
}

