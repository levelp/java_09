<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Вход в систему</title>
</head>
<body>


<form action="${pageContext.request.contextPath}/login" method="post">
    <div>
        <label for="login">
            Логин:
            <input id="login" type="text" name="login" value="${param["login"]}"/>
        </label>
    </div>

    <div>
        <label for="password">
            Пароль:
            <input id="password" type="password" name="password"/>
        </label>
    </div>

    <input type="submit" name="submit"/>
</form>

</body>
</html>
