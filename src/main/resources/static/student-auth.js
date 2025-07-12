// DOM 요소
const authForm = document.getElementById("studentAuthForm")
const authStudentIdInput = document.getElementById("authStudentId")
const authPasswordInput = document.getElementById("authPassword")
const agreeAuthCheckbox = document.getElementById("agreeAuth")
const authBtn = document.getElementById("authBtn")
const authText = document.getElementById("authText")
const authSpinner = document.getElementById("authSpinner")
const authFormContainer = document.getElementById("authForm")
const successScreen = document.getElementById("successScreen")

// 초기화
document.addEventListener("DOMContentLoaded", () => {
  setupEventListeners()
})

// 이벤트 리스너 설정
function setupEventListeners() {
  authForm.addEventListener("submit", handleAuth)
  agreeAuthCheckbox.addEventListener("change", updateAuthButton)
}

// 인증 버튼 상태 업데이트
function updateAuthButton() {
  authBtn.disabled = !agreeAuthCheckbox.checked
}

// 학생 인증 처리
async function handleAuth(e) {
  e.preventDefault()

  const studentId = authStudentIdInput.value.trim()
  const password = authPasswordInput.value.trim()
  const agreed = agreeAuthCheckbox.checked

  // 유효성 검사
  if (!studentId || !password) {
    alert("학번과 비밀번호를 모두 입력해주세요.")
    return
  }

  if (!agreed) {
    alert("학생 정보 확인에 동의해주세요.")
    return
  }

  // 로딩 상태 시작
  setAuthLoadingState(true)

  try {
    // 인증 API 호출 시뮬레이션
    await simulateAuth(studentId, password)

    // 성공 화면 표시
    showSuccessScreen()
  } catch (error) {
    alert("인증에 실패했습니다. 학번과 비밀번호를 확인해주세요.")
  } finally {
    setAuthLoadingState(false)
  }
}

// 인증 시뮬레이션
function simulateAuth(studentId, password) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      // 간단한 유효성 검사 (실제로는 학교 포털과 연동)
      if (studentId.length >= 8 && password.length >= 4) {
        resolve()
      } else {
        reject(new Error("Invalid credentials"))
      }
    }, 2000)
  })
}

// 로딩 상태 설정
function setAuthLoadingState(isLoading) {
  authBtn.disabled = isLoading || !agreeAuthCheckbox.checked

  if (isLoading) {
    authText.textContent = "인증 중..."
    authSpinner.style.display = "inline-block"
  } else {
    authText.textContent = "인증하기"
    authSpinner.style.display = "none"
  }
}

// 성공 화면 표시
function showSuccessScreen() {
  authFormContainer.style.display = "none"
  successScreen.style.display = "flex"
}
