import {createClient} from "https://cdn.jsdelivr.net/npm/@supabase/supabase-js/+esm";

const SUPABASE_URL = "https://sbxlfgdwogpdondzyvia.supabase.co";
const SUPABASE_KEY = "sb_publishable_XHbMwByaC6Niq019201IXA_CBg83ih0";

const supabase = createClient(SUPABASE_URL, SUPABASE_KEY);

const loginModal = document.querySelector("#loginModal");
const signupModal = document.querySelector("#signupModal");

const openLoginModalButton = document.querySelector("#openLoginModal");
const openSignupModalButton = document.querySelector("#openSignupModal");

const closeLoginModalButton = document.querySelector("#closeLoginModal");
const closeSignupModalButton = document.querySelector("#closeSignupModal");

const loginForm = document.querySelector("#loginForm");
const signupForm = document.querySelector("#signupForm");

const loginMessage = document.querySelector("#loginMessage");
const signupMessage = document.querySelector("#signupMessage");

function showMessage(messageElement, message) {
    messageElement.textContent = message;
}

function clearMessage(messageElement) {
    messageElement.textContent = "";
}

function resetLoginForm() {
    loginForm.reset();
    clearMessage(loginMessage);
}

function resetSignupForm() {
    signupForm.reset();
    clearMessage(signupMessage);
}

function getLoginErrorMessage(error) {
    const message = error.message.toLowerCase();

    if (message.includes("invalid login credentials")) {
        return "이메일 또는 비밀번호가 올바르지 않습니다.";
    }

    if (message.includes("email not confirmed")) {
        return "이메일 인증이 아직 완료되지 않았습니다.";
    }

    return "로그인에 실패하였습니다. 입력 정보를 다시 확인해 주십시오.";
}

function getSignupErrorMessage(error) {
    const message = error.message.toLowerCase();

    if (message.includes("already registered") || message.includes("already exists")) {
        return "이미 가입된 이메일입니다.";
    }

    if (message.includes("password")) {
        return "비밀번호 조건을 다시 확인해 주십시오.";
    }

    if (message.includes("email")) {
        return "이메일 형식을 다시 확인해 주십시오.";
    }

    return "회원가입에 실패했습니다. 잠시 후 다시 시도해 주십시오.";
}

openLoginModalButton.addEventListener("click", () => {
    resetLoginForm();
    loginModal.showModal();
});

openSignupModalButton.addEventListener("click", () => {
    resetSignupForm();
    signupModal.showModal();
});

closeLoginModalButton.addEventListener("click", () => {
    loginModal.close();
});

closeSignupModalButton.addEventListener("click", () => {
    signupModal.close();
});

loginModal.addEventListener("close", () => {
    resetLoginForm();
});

signupModal.addEventListener("close", () => {
    resetSignupForm();
});

// 로그인
loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const email = document.querySelector("#loginEmail").value;
    const password = document.querySelector("#loginPassword").value;

    const {data, error} = await supabase.auth.signInWithPassword({
        email,
        password
    });

    if (error) {
        showMessage(loginMessage, getLoginErrorMessage(error));
        return;
    }

    if (!data.session || !data.session.access_token) {
        console.log("Supabase login data:", data);
        showMessage(loginMessage, "Supabase 로그인 세션을 가져오지 못했습니다.");
        return;
    }

    const accessToken = data.session.access_token;

    console.log("accessToken length:", accessToken.length);
    console.log("accessToken startsWith eyJ:", accessToken.startsWith("eyJ"));

    const response = await fetch(`${window.APP_CONTEXT_PATH}/auth/session`, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({
            accessToken: accessToken
        })
    });

    if (!response.ok) {
        alert("서버 로그인 세션 저장 실패.");
        return;
    }

    const chatUrl = `${window.APP_CONTEXT_PATH || ""}/chat`;
    location.href = chatUrl;
});

// 회원가입
signupForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const email = document.querySelector("#signupEmail").value;
    const password = document.querySelector("#signupPassword").value;

    const {data, error} = await supabase.auth.signUp({
        email,
        password
    });

    if (error) {
        signupMessage.classList.remove("success");
        showMessage(signupMessage, getSignupErrorMessage(error));
        return;
    }

    console.log("회원가입 성공:", data);
    signupMessage.classList.add("success");
    showMessage(signupMessage, "회원가입이 완료되었습니다. 로그인을 해주세요.");
    signupForm.reset();
});