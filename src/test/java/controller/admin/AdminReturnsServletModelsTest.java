package controller.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class AdminReturnsServletModelsTest {

    @Test
    @DisplayName("RefundOrderVM getters/setters work")
    void refundOrderVM_getters_setters() {
        AdminReturnsServlet.RefundOrderVM vm = new AdminReturnsServlet.RefundOrderVM();

        int orderId = 123;
        String customerName = "Alice";
        String customerPhone = "090";
        String bikeName = "Yamaha";
        Date endDate = new Date();
        BigDecimal deposit = new BigDecimal("150.50");
        String returnStatus = "returned";
        Date returnedAt = new Date(endDate.getTime() + 1000);
        String depositStatus = "held";

        vm.setOrderId(orderId);
        vm.setCustomerName(customerName);
        vm.setCustomerPhone(customerPhone);
        vm.setBikeName(bikeName);
        vm.setEndDate(endDate);
        vm.setDepositAmount(deposit);
        vm.setReturnStatus(returnStatus);
        vm.setReturnedAt(returnedAt);
        vm.setDepositStatus(depositStatus);

        assertThat(vm.getOrderId()).isEqualTo(orderId);
        assertThat(vm.getCustomerName()).isEqualTo(customerName);
        assertThat(vm.getCustomerPhone()).isEqualTo(customerPhone);
        assertThat(vm.getBikeName()).isEqualTo(bikeName);
        assertThat(vm.getEndDate()).isEqualTo(endDate);
        assertThat(vm.getDepositAmount()).isEqualByComparingTo(deposit);
        assertThat(vm.getReturnStatus()).isEqualTo(returnStatus);
        assertThat(vm.getReturnedAt()).isEqualTo(returnedAt);
        assertThat(vm.getDepositStatus()).isEqualTo(depositStatus);
    }
}

