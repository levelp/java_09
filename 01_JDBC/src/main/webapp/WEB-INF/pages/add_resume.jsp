<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Добавить новое резюме</title>
</head>
<body>

<h2>Добавить новое резюме</h2>

<form action="${pageContext.request.contextPath}/addResume">

    <label>
        <input type="text" name="name" value="${resume.name}"/>
    </label>

    <input type="submit" value="Сохранить"/>

</form>

</body>
</html>
