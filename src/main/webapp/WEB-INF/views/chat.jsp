<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ArChat</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css?v=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/chat.css?v=2">
</head>
<body>
<c:set var="activeTitle" value="New chat"/>
<c:forEach var="conversation" items="${conversations}">
    <c:if test="${conversation.conversationId eq activeConversationId}">
        <c:set var="activeTitle" value="${conversation.title}"/>
    </c:if>
</c:forEach>
<div class="app">
    <aside class="sidebar">
        <div class="brand">
            <div class="brand-mark">AR</div>
            <span>ArChat</span></div>
        <form class="new-chat-form" action="<c:url value='/chat'/>" method="post">
            <input type="hidden" name="action" value="create">
            <button class="new-chat" type="submit">+ New chat</button>
        </form>
        <div class="chat-list-title">Chats</div>
        <div class="chat-list">
            <c:choose>
                <c:when test="${empty conversations}">
                    <div class="chat-card active"><strong>New chat</strong><span>Your first message becomes the title of this chat.</span>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="conversation" items="${conversations}">
                        <article
                                class="chat-card ${conversation.conversationId eq activeConversationId ? 'active' : ''}">
                            <a class="chat-link"
                               href="${pageContext.request.contextPath}/chat?conversationId=${conversation.conversationId}">
                                <strong>${conversation.title}</strong><span>${conversation.updatedAt}</span>
                            </a>
                            <c:if test="${conversation.conversationId eq activeConversationId}">
                                <div class="chat-actions">
                                    <button class="chat-action-button" type="button"
                                            data-rename-target="rename-${conversation.conversationId}">Rename
                                    </button>
                                    <form action="<c:url value='/chat'/>" method="post">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="conversationId"
                                               value="${conversation.conversationId}">
                                        <button class="chat-action-submit" type="submit">Delete</button>
                                    </form>
                                </div>
                                <form id="rename-${conversation.conversationId}" class="rename-form"
                                      action="<c:url value='/chat'/>" method="post">
                                    <input type="hidden" name="action" value="rename">
                                    <input type="hidden" name="conversationId" value="${conversation.conversationId}">
                                    <input type="text" name="title" value="${conversation.title}" maxlength="48"
                                           required>
                                    <div class="rename-controls">
                                        <button type="submit">Save</button>
                                        <button type="button" data-close-rename="rename-${conversation.conversationId}">
                                            Cancel
                                        </button>
                                    </div>
                                </form>
                            </c:if>
                        </article>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </aside>
    <main class="main">
        <header class="header">
            <div class="header-title">
                <h1>${activeTitle}</h1>
                <p>Conversation list and message history stay separated.</p>
            </div>
            <form id="logoutForm" class="logout-form" action="<c:url value='/auth/logout'/>" method="post">
                <button type="submit" class="logout-button">Log out</button>
            </form>
        </header>
        <section class="messages" id="messages">
            <c:choose>
                <c:when test="${empty chats}">
                    <div class="empty"><h2>What can I help with?</h2>
                        <p>This screen is wired for conversation-based storage. Send a first message to create a new
                            chat and show it in the left sidebar.</p></div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="chat" items="${chats}">
                        <article class="message ${chat.owner eq 'USER' ? 'user' : 'assistant'}">
                            <div class="message-meta">
                                <strong>${chat.owner eq 'USER' ? 'You' : 'ArChat'}</strong><span>${chat.model} | ${chat.timestamp}</span>
                            </div>
                            <div class="message-body">${chat.message}</div>
                            <c:if test="${not empty chat.attachments}">
                                <div class="message-attachments">
                                    <c:forEach var="attachment" items="${chat.attachments}">
                                        <c:choose>
                                            <c:when test="${attachment.image}">
                                                <a class="message-image-link" href="${attachment.filePath}"
                                                   target="_blank" rel="noopener">
                                                    <img class="message-image" src="${attachment.filePath}"
                                                         alt="${attachment.fileName}">
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <a class="message-file" href="${attachment.filePath}" target="_blank"
                                                   rel="noopener">
                                                    <span>${attachment.fileName}</span>
                                                </a>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </div>
                            </c:if>
                        </article>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </section>
        <div class="composer-wrap">
            <div class="composer">
                <form class="composer-form" action="<c:url value='/chat'/>" method="post" enctype="multipart/form-data">
                    <input type="hidden" name="conversationId" value="${activeConversationId}">
                    <div class="composer-main"><textarea id="message" name="message" placeholder="Type your message"
                                                         required></textarea></div>
                    <div class="attachments" id="attachmentPreview"></div>
                    <div class="composer-bar">
                        <div class="composer-left">
                            <label class="attach-button"
                                   for="attachmentInput"><span>+</span><span>Add files</span><input id="attachmentInput"
                                                                                                    type="file"
                                                                                                    name="attachments"
                                                                                                    multiple
                                                                                                    accept="image/*,.pdf,.txt,.doc,.docx,.ppt,.pptx,.xls,.xlsx"></label>
                            <span class="helper-text">Images show preview before sending and appear inline after upload.</span>
                        </div>
                        <div class="composer-right">
                            <select name="model">
                                <optgroup label="Google AI">
                                    <option value="gemma-4-26b-a4b-it">gemma-4-26b-a4b-it</option>
                                    <option value="gemma-4-31b-it">gemma-4-31b-it</option>
                                    <option value="gemini-3.1-flash-lite">gemini-3.1-flash-lite</option>
                                </optgroup>
                                <optgroup label="Groq">
                                    <option value="openai/gpt-oss-20b">openai/gpt-oss-20b</option>
                                    <option value="openai/gpt-oss-120b">openai/gpt-oss-120b</option>
                                    <option value="llama-3.1-8b-instant">llama-3.1-8b-instant</option>
                                    <option value="llama-3.3-70b-versatile">llama-3.3-70b-versatile</option>
                                    <option value="groq/compound-mini">groq/compound-mini</option>
                                    <option value="groq/compound">groq/compound</option>
                                    <option value="qwen/qwen3-32b">qwen/qwen3-32b</option>
                                </optgroup>
                                <optgroup label="NVIDIA NIM">
                                    <option value="nvidia/nemotron-3-ultra-550b-a55b">
                                        nvidia/nemotron-3-ultra-550b-a55b
                                    </option>
                                    <option value="deepseek-ai/deepseek-v4-pro">deepseek-ai/deepseek-v4-pro</option>
                                </optgroup>
                            </select>
                            <button class="send-button" type="submit">Send</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </main>
</div>
<script src="${pageContext.request.contextPath}/js/chat.js?v=1"></script>
<script type="module" src="${pageContext.request.contextPath}/js/logout.js?v=1"></script>
</body>
</body>
</html>
