<%-- WEB-INF/views/chat.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<html>
<head>
    <title>AI 챗봇</title>
</head>
<body>
<header>
    <h1>Archat</h1>
    <form action="<c:url value="/auth/logout"/>" method="post">
        <button type="submit">로그아웃</button>
    </form>
</header>

<form action="<c:url value="/chat"/>" method="post">
    <input name="message" placeholder="메시지를 입력하세요"/>
    <select name="model">
        <optgroup label="Google AI">
            <option value="gemma-4-26b-a4b-it">gemma-4-26b-a4b-it</option>
            <option value="gemma-4-31b-it">gemma-4-31b-it</option>
            <option value="gemini-3.1-flash-lite">gemini-3.1-flash-lite</option>
        </optgroup>
        <optgroup label="Groq - Production">
            <option value="openai/gpt-oss-20b">openai/gpt-oss-20b</option>
            <option value="openai/gpt-oss-120b">openai/gpt-oss-120b</option>
            <option value="llama-3.1-8b-instant">llama-3.1-8b-instant</option>
            <option value="llama-3.3-70b-versatile">llama-3.3-70b-versatile</option>
            <option value="groq/compound-mini">groq/compound-mini</option>
            <option value="groq/compound">groq/compound</option>
        </optgroup>
        <optgroup label="Groq - Preview">
            <option value="qwen/qwen3-32b">qwen/qwen3-32b</option>
        </optgroup>
        <optgroup label="NVIDIA NIM">
            <option value="nvidia/nemotron-3-ultra-550b-a55b">
                Nemotron 3 Ultra
            </option>

            <option value="deepseek-ai/deepseek-v4-pro">
                DeepSeek V4 Pro
            </option>
        </optgroup>
    </select>
    <button>전송</button>
</form>
<section>
    <c:if test="${empty chats}">
        <p>아직 채팅이 없습니다</p>
    </c:if>
    <c:forEach var="chat" items="${chats}">
        <div>
            <ul>
                <li><strong>${chat.owner}</strong></li>
                <li>모델 : ${chat.model}</li>
                <li>${chat.message}</li>
                <li>작성일시 : ${chat.timestamp}</li>
            </ul>
        </div>
    </c:forEach>
</section>
</body>
</html>
