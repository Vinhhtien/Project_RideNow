package controller.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

class AdminOrderDetailServletModelsTest {

    @Test
    @DisplayName("RefundInfoDTO getters/setters work")
    void refundInfoDTO_getters_setters() {
        AdminOrderDetailServlet.RefundInfoDTO dto = new AdminOrderDetailServlet.RefundInfoDTO();

        BigDecimal refundAmount = new BigDecimal("200.00");
        String refundMethod = "wallet";
        String refundStatus = "completed";
        String bikeCondition = "good";
        BigDecimal damageFee = new BigDecimal("10.00");
        String damageNotes = "scratch";
        String adminNotes = "checked";
        Timestamp inspectedAt = new Timestamp(System.currentTimeMillis());

        dto.setRefundAmount(refundAmount);
        dto.setRefundMethod(refundMethod);
        dto.setRefundStatus(refundStatus);
        dto.setBikeCondition(bikeCondition);
        dto.setDamageFee(damageFee);
        dto.setDamageNotes(damageNotes);
        dto.setAdminNotes(adminNotes);
        dto.setInspectedAt(inspectedAt);

        assertThat(dto.getRefundAmount()).isEqualByComparingTo(refundAmount);
        assertThat(dto.getRefundMethod()).isEqualTo(refundMethod);
        assertThat(dto.getRefundStatus()).isEqualTo(refundStatus);
        assertThat(dto.getBikeCondition()).isEqualTo(bikeCondition);
        assertThat(dto.getDamageFee()).isEqualByComparingTo(damageFee);
        assertThat(dto.getDamageNotes()).isEqualTo(damageNotes);
        assertThat(dto.getAdminNotes()).isEqualTo(adminNotes);
        assertThat(dto.getInspectedAt()).isEqualTo(inspectedAt);
    }
}

