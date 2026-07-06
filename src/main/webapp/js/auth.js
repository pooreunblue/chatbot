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

function resetLoginForm() {
    loginForm.reset();
}

function resetSignupForm() {
    signupForm.reset();
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
        alert(error.message);
        return;
    }

    if (!data.session || !data.session.access_token) {
        console.log("Supabase login data:", data);
        alert("Supabase 로그인 세션을 가져오지 못했어.");
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

    alert("로그인 성공!");

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
        alert(error.message);
        return;
    }

    console.log("회원가입 성공:", data);
    alert("회원가입 성공! 이메일 인증 설정이 켜져 있다면 메일 인증 후 로그인하면 돼.");

    signupModal.close();
});