<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ArChat</title>
    <style>
        :root { --bg:#f7f7f8; --panel:#ffffff; --panel-soft:#fbfbfc; --line:#e7e7ea; --line-strong:#dcdce1; --text:#161616; --muted:#6e6e73; --hover:#f1f1f3; --danger:#c83d3d; }
        * { box-sizing:border-box; } body { margin:0; min-height:100vh; background:var(--bg); color:var(--text); font-family:"Segoe UI","Malgun Gothic",sans-serif; }
        .app { display:grid; grid-template-columns:300px 1fr; min-height:100vh; } .sidebar { background:#fcfcfd; border-right:1px solid var(--line); padding:16px 14px; display:flex; flex-direction:column; gap:14px; }
        .brand { display:flex; align-items:center; gap:10px; padding:8px 6px; } .brand-mark { width:32px; height:32px; border-radius:10px; background:#111; color:#fff; display:grid; place-items:center; font-size:.8rem; font-weight:700; } .brand span { font-size:.95rem; font-weight:600; }
        .new-chat-form { margin:0; } .new-chat { width:100%; border:1px solid var(--line); background:#fff; color:var(--text); border-radius:14px; padding:12px 14px; text-align:left; font-size:.94rem; font-weight:600; cursor:pointer; }
        .chat-list-title { padding:6px 8px 0; font-size:.76rem; color:var(--muted); text-transform:uppercase; letter-spacing:.06em; } .chat-list { display:flex; flex-direction:column; gap:8px; }
        .chat-card { padding:12px; border-radius:14px; border:1px solid transparent; background:transparent; } .chat-card.active { background:#f1f1f3; border-color:#e4e4e8; } .chat-link { text-decoration:none; color:inherit; display:block; }
        .chat-card strong,.chat-card span { display:block; } .chat-card strong { font-size:.9rem; font-weight:600; line-height:1.35; color:var(--text); } .chat-card span { margin-top:5px; font-size:.8rem; color:var(--muted); line-height:1.4; }
        .chat-actions { display:flex; gap:8px; margin-top:10px; } .chat-actions form { margin:0; flex:1; }
        .chat-action-button,.chat-action-submit { width:100%; border-radius:10px; padding:8px 10px; font-size:.78rem; cursor:pointer; } .chat-action-button { border:1px solid var(--line); background:#fff; color:var(--text); } .chat-action-submit { border:1px solid #f1c8c8; background:#fff6f6; color:var(--danger); }
        .rename-form { display:none; margin-top:10px; } .rename-form.active { display:block; } .rename-form input { width:100%; border:1px solid var(--line); border-radius:10px; padding:9px 10px; font:inherit; margin-bottom:8px; } .rename-controls { display:flex; gap:8px; } .rename-controls button { flex:1; border-radius:10px; border:1px solid var(--line); background:#fff; padding:8px 10px; cursor:pointer; }
        .main { display:grid; grid-template-rows:auto 1fr auto; min-width:0; } .header { min-height:64px; padding:16px 24px; border-bottom:1px solid var(--line); background:rgba(255,255,255,.92); display:flex; align-items:center; justify-content:space-between; gap:12px; }
        .header-title h1 { margin:0; font-size:1rem; font-weight:600; } .header-title p { margin:4px 0 0; font-size:.82rem; color:var(--muted); } .header-badge { padding:8px 10px; border-radius:999px; border:1px solid var(--line); background:#fff; color:var(--muted); font-size:.8rem; white-space:nowrap; }
        .messages { overflow-y:auto; padding:28px 0 36px; } .empty { max-width:720px; margin:80px auto 0; padding:0 24px; text-align:center; } .empty h2 { margin:0; font-size:1.8rem; font-weight:600; } .empty p { margin:12px auto 0; max-width:560px; line-height:1.7; color:var(--muted); }
        .message { max-width:860px; margin:0 auto; padding:0 24px; display:flex; flex-direction:column; } .message + .message { margin-top:24px; } .message-meta { display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:10px; font-size:.8rem; color:var(--muted); } .message-meta strong { color:var(--text); font-size:.9rem; }
        .message-body { width:fit-content; max-width:min(100%, 760px); border:1px solid var(--line); border-radius:20px; padding:16px 18px; line-height:1.7; white-space:pre-wrap; word-break:break-word; background:#fff; box-shadow:0 1px 2px rgba(0,0,0,.03); } .message.user { align-items:flex-end; } .message.user .message-body { background:#111; color:#fff; border-color:#111; } .message.assistant { align-items:flex-start; } .message.assistant .message-body { background:#fff; }
        .message.user .message-meta { width:fit-content; align-self:flex-end; }
        .message.assistant .message-meta { width:fit-content; align-self:flex-start; }
        .composer-wrap { padding:18px 24px 24px; border-top:1px solid var(--line); background:#fff; } .composer { max-width:860px; margin:0 auto; } .composer-form { border:1px solid var(--line-strong); border-radius:24px; background:#fff; box-shadow:0 10px 30px rgba(0,0,0,.04); overflow:hidden; }
        .composer-main { padding:16px 18px 12px; } .composer-main textarea { width:100%; min-height:92px; resize:vertical; border:0; outline:none; font:inherit; color:var(--text); background:transparent; }
        .attachments { display:flex; flex-wrap:wrap; gap:8px; padding:0 18px 14px; } .attachments:empty { display:none; } .attachment-chip { display:inline-flex; align-items:center; gap:8px; padding:8px 10px; border-radius:12px; background:#f4f4f6; border:1px solid #e6e6ea; font-size:.82rem; color:#3d3d42; max-width:100%; } .attachment-thumb { width:42px; height:42px; object-fit:cover; border-radius:10px; flex:0 0 auto; }
        .message-attachments { display:flex; flex-direction:column; gap:10px; margin-top:14px; } .message-image-link,.message-file { display:block; text-decoration:none; } .message-image { display:block; max-width:100%; width:auto; max-height:360px; border-radius:16px; border:1px solid var(--line); object-fit:contain; background:#fff; } .message-file { padding:12px 14px; border:1px solid var(--line); border-radius:14px; color:inherit; background:#fafafa; }
        .composer-bar { display:flex; align-items:center; justify-content:space-between; gap:12px; flex-wrap:wrap; padding:14px 18px 18px; border-top:1px solid var(--line); background:var(--panel-soft); } .composer-left,.composer-right { display:flex; align-items:center; gap:10px; flex-wrap:wrap; }
        .attach-button,.composer-bar select,.send-button { border-radius:999px; font:inherit; } .attach-button { display:inline-flex; align-items:center; gap:8px; padding:10px 12px; border:1px solid var(--line); background:#fff; cursor:pointer; color:var(--text); } .attach-button input { display:none; } .composer-bar select { border:1px solid var(--line); background:#fff; color:var(--text); padding:10px 12px; } .send-button { border:0; background:#111; color:#fff; padding:11px 16px; font-weight:600; cursor:pointer; } .helper-text { font-size:.8rem; color:var(--muted); }
        @media (max-width:920px) { .app { grid-template-columns:1fr; } .sidebar { border-right:0; border-bottom:1px solid var(--line); } .header { padding:16px; } .message,.composer-wrap { padding-left:16px; padding-right:16px; } }
    </style>
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
<script>
    (function () {
        const attachmentInput = document.getElementById("attachmentInput");
        const attachmentPreview = document.getElementById("attachmentPreview");
        const messages = document.getElementById("messages");
        const composerForm = document.querySelector(".composer-form");
        const messageInput = document.getElementById("message");
        const sendButton = document.querySelector(".send-button");
        let previewUrls = [];

        function renderFiles(files) {
            previewUrls.forEach(function (url) {
                URL.revokeObjectURL(url);
            });
            previewUrls = [];
            attachmentPreview.innerHTML = "";
            if (!files || files.length === 0) return;
            Array.from(files).forEach(function (file) {
                const chip = document.createElement("div");
                chip.className = "attachment-chip";
                if (file.type && file.type.indexOf("image/") === 0) {
                    const url = URL.createObjectURL(file);
                    previewUrls.push(url);
                    const img = document.createElement("img");
                    img.className = "attachment-thumb";
                    img.src = url;
                    img.alt = file.name;
                    chip.appendChild(img);
                }
                const label = document.createElement("span");
                label.textContent = file.name;
                chip.appendChild(label);
                attachmentPreview.appendChild(chip);
            });
        }
        function appendMessage(role, text, modelLabel) {
            const article = document.createElement("article");
            article.className = "message " + (role === "USER" ? "user" : "assistant");

            const meta = document.createElement("div");
            meta.className = "message-meta";

            const strong = document.createElement("strong");
            strong.textContent = role === "USER" ? "You" : "ArChat";

            const span = document.createElement("span");
            span.textContent = modelLabel || (role === "USER" ? "Sending..." : "Thinking...");

            meta.appendChild(strong);
            meta.appendChild(span);

            const body = document.createElement("div");
            body.className = "message-body";
            body.textContent = text;

            article.appendChild(meta);
            article.appendChild(body);
            messages.appendChild(article);
            messages.scrollTop = messages.scrollHeight;
            return article;
        }
        attachmentInput.addEventListener("change", function (event) { renderFiles(event.target.files); });
        document.querySelectorAll("[data-rename-target]").forEach(function (button) {
            button.addEventListener("click", function () {
                const form = document.getElementById(button.dataset.renameTarget);
                form.classList.toggle("active");
                const input = form.querySelector("input[name='title']");
                if (form.classList.contains("active")) {
                    input.focus();
                    input.select();
                }
            });
        });
        document.querySelectorAll("[data-close-rename]").forEach(function (button) {
            button.addEventListener("click", function () {
                const form = document.getElementById(button.dataset.closeRename);
                form.classList.remove("active");
            });
        });
        composerForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const text = messageInput.value;
            if (!text || !text.trim()) {
                return;
            }

            appendMessage("USER", text, "Sending...");
            appendMessage("AI", "Generating response...", "Thinking...");

            const formData = new FormData(composerForm);
            sendButton.disabled = true;
            messageInput.disabled = true;

            try {
                const response = await fetch(composerForm.action, {
                    method: "POST",
                    body: formData
                });

                if (!response.ok) {
                    throw new Error("Failed to send message");
                }

                window.location.href = response.url || (window.location.pathname + window.location.search);
            } catch (error) {
                const pending = messages.querySelector(".message.assistant:last-of-type");
                if (pending && pending.textContent.indexOf("Generating response...") !== -1) {
                    pending.remove();
                }
                alert("메시지 전송에 실패했습니다.");
            } finally {
                sendButton.disabled = false;
                messageInput.disabled = false;
                messageInput.value = "";
                attachmentInput.value = "";
                attachmentPreview.innerHTML = "";
            }
        });
        messages.scrollTop = messages.scrollHeight;
    })();
</script>
<script type="module" src="${pageContext.request.contextPath}/js/logout.js?v=1"></script>
</body>
</body>
</html>
