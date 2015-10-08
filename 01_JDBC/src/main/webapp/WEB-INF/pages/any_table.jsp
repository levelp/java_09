<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Произвольная таблица</title>
</head>
<body>

<h2>Произвольная таблица</h2>

<%
    int[][] result = (int[][]) request.getAttribute("result");
    int size = (int) request.getAttribute("size"); %>
Размер таблицы = <%=size %>

<table border="1">
    <% for (int i = 0; i < result.length; ++i) { /* Цикл по строчкам */ %>
    <tr>
        <% for (int j = 0; j < result.length; ++j) { /* Цикл по столбцам */ %>
        <td>
            <%=result[i][j]%>
        </td>
        <% } %>
    </tr>
    <% } %>
</table>

</body>
</html>
