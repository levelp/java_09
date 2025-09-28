<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Поиск по сайту</title>
</head>
<body>

<h1>Вы вошли на сайт! ${userName}</h1>

<h2>Счётчик: ${count}</h2>

<a href="${pageContext.request.contextPath}/logout">Выйти из системы</a>


TODO: поиск по вакансиям

<form action="${pageContext.request.contextPath}/search">
    <label>
        Название должности:
        <input name="search"/>
        <input name="doSearch" type="submit" value="Искать!"/>
    </label>
</form>


</body>
</html>
