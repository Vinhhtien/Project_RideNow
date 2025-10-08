<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Motorbike" %>
<html>
<head>
    <title>Xe của tôi</title>
    <style>
        body { font-family: Arial; background-color: #f9f9f9; }
        table { width: 80%; margin: auto; border-collapse: collapse; margin-top: 40px; }
        th, td { padding: 10px; border: 1px solid #ccc; text-align: left; }
        th { background-color: #6a5acd; color: white; }
        h2 { text-align: center; margin-top: 30px; }
        p { text-align: center; }
    </style>
</head>
<body>
    <h2>🚲 Danh sách xe đang đăng bán</h2>

    <%
        List<Motorbike> motorbikes = (List<Motorbike>) request.getAttribute("motorbikes");
        if (motorbikes != null && !motorbikes.isEmpty()) {
    %>
        <p style="text-align:center;">Tổng số xe: <b><%= motorbikes.size() %></b></p>
        <table>
            <tr>
                <th>Tên xe</th>
                <th>Biển số</th>
                <th>Giá/ngày</th>
                <th>Trạng thái</th>
                <th>Mô tả</th>
            </tr>
            <% for (Motorbike bike : motorbikes) { %>
                <tr>
                    <td><%= bike.getBikeName() %></td>
                    <td><%= bike.getLicensePlate() %></td>
                    <td><%= bike.getPricePerDay() %> VNĐ</td>
                    <td><%= bike.getStatus() %></td>
                    <td><%= bike.getDescription() %></td>
                </tr>
            <% } %>
        </table>
    <% } else { %>
        <p>Bạn chưa đăng bán xe nào.</p>
    <% } %>
</body>
</html>
