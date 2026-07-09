(function () {
    const attachmentInput = document.getElementById("attachmentInput");
    const attachmentPreview = document.getElementById("attachmentPreview");
    const messages = document.getElementById("messages");
    const composerForm = document.querySelector(".composer-form");
    const messageInput = document.getElementById("message");
    const sendButton = document.querySelector(".send-button");
    const scrollTopButton = document.getElementById("scrollTopButton");
    const scrollBottomButton = document.getElementById("scrollBottomButton");

    if (!messages) {
        return;
    }

    if ("scrollRestoration" in history) {
        history.scrollRestoration = "manual";
    }

    let previewUrls = [];

    function scrollMessagesToBottom() {
        const lastMessage = messages.lastElementChild;

        if (lastMessage && typeof lastMessage.scrollIntoView === "function") {
            lastMessage.scrollIntoView({ block: "end" });
            return;
        }

        messages.scrollTop = messages.scrollHeight;
    }

    const messageObserver = new MutationObserver(function () {
        requestAnimationFrame(scrollMessagesToBottom);
    });
    messageObserver.observe(messages, { childList: true, subtree: true });

    function createAttachmentList(files) {
        const container = document.createElement("div");
        container.className = "message-attachments";

        Array.from(files || []).forEach(function (file) {
            const item = document.createElement("div");
            item.className = "attachment-chip";

            if (file.type && file.type.indexOf("image/") === 0) {
                const url = URL.createObjectURL(file);
                previewUrls.push(url);

                const link = document.createElement("a");
                link.className = "message-image-link";
                link.href = url;
                link.target = "_blank";
                link.rel = "noopener";

                const img = document.createElement("img");
                img.className = "message-image";
                img.src = url;
                img.alt = file.name;

                link.appendChild(img);
                item.appendChild(link);
            }

            const label = document.createElement("span");
            label.textContent = file.name;

            item.appendChild(label);
            container.appendChild(item);
        });

        return container;
    }

    function renderFiles(files) {
        if (!attachmentPreview) {
            return;
        }

        previewUrls.forEach(function (url) {
            URL.revokeObjectURL(url);
        });

        previewUrls = [];
        attachmentPreview.innerHTML = "";

        if (!files || files.length === 0) {
            return;
        }

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
        scrollMessagesToBottom();

        return article;
    }

    function appendPendingUserMessage(text, files) {
        const article = document.createElement("article");
        article.className = "message user pending";

        const meta = document.createElement("div");
        meta.className = "message-meta";

        const strong = document.createElement("strong");
        strong.textContent = "You";

        const span = document.createElement("span");
        span.textContent = "Sending...";

        meta.appendChild(strong);
        meta.appendChild(span);
        article.appendChild(meta);

        if (files && files.length > 0) {
            article.appendChild(createAttachmentList(files));
        }

        const body = document.createElement("div");
        body.className = "message-body";
        body.textContent = text;
        article.appendChild(body);

        messages.appendChild(article);
        scrollMessagesToBottom();

        return article;
    }

    if (attachmentInput) {
        attachmentInput.addEventListener("change", function (event) {
            renderFiles(event.target.files);
        });
    }

    if (scrollTopButton) {
        scrollTopButton.addEventListener("click", function () {
            window.scrollTo({ top: 0, behavior: "smooth" });
            messages.scrollTo({ top: 0, behavior: "smooth" });

            const firstMessage = messages.firstElementChild;
            if (firstMessage && typeof firstMessage.scrollIntoView === "function") {
                firstMessage.scrollIntoView({ block: "start", behavior: "smooth" });
            }
        });
    }

    if (scrollBottomButton) {
        scrollBottomButton.addEventListener("click", function () {
            window.scrollTo({ top: document.documentElement.scrollHeight, behavior: "smooth" });
            scrollMessagesToBottom();
        });
    }

    document.querySelectorAll("[data-rename-target]").forEach(function (button) {
        button.addEventListener("click", function () {
            const form = document.getElementById(button.dataset.renameTarget);

            if (!form) {
                return;
            }

            form.classList.toggle("active");

            const input = form.querySelector("input[name='title']");

            if (form.classList.contains("active") && input) {
                input.focus();
                input.select();
            }
        });
    });

    document.querySelectorAll("[data-close-rename]").forEach(function (button) {
        button.addEventListener("click", function () {
            const form = document.getElementById(button.dataset.closeRename);

            if (!form) {
                return;
            }

            form.classList.remove("active");
        });
    });

    if (composerForm && messageInput && sendButton) {
        composerForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const text = messageInput.value;
            const files = attachmentInput ? attachmentInput.files : null;

            if ((!text || !text.trim()) && (!files || files.length === 0)) {
                return;
            }

            const pendingUserMessage = appendPendingUserMessage(text, files);
            appendMessage("AI", "Generating response...", "Thinking...");

            const formData = new FormData(composerForm);

            messageInput.value = "";
            if (attachmentInput) {
                attachmentInput.value = "";
            }
            if (attachmentPreview) {
                attachmentPreview.innerHTML = "";
            }

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
            }
        });
    }

    requestAnimationFrame(function () {
        scrollMessagesToBottom();
        requestAnimationFrame(scrollMessagesToBottom);
    });
    window.addEventListener("load", function () {
        setTimeout(scrollMessagesToBottom, 0);
        setTimeout(scrollMessagesToBottom, 100);
        setTimeout(scrollMessagesToBottom, 250);
    });
    window.addEventListener("pageshow", function () {
        requestAnimationFrame(scrollMessagesToBottom);
    });
})();
