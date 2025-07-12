// src/main/resources/static/application-form.js

document.addEventListener('DOMContentLoaded', function() {
    // 파일 이름 업데이트 함수 정의
    function updateFileName(inputElement) { // 매개변수 이름을 'inputElement'로 변경하여 혼동 방지
        const fileNameSpan = document.getElementById('selectedFileName');
        if (inputElement.files && inputElement.files.length > 0) {
            fileNameSpan.textContent = inputElement.files[0].name;
        } else {
            fileNameSpan.textContent = '선택된 파일 없음';
        }
    }

    // 파일 선택 버튼 요소 가져오기
    const fileSelectButton = document.getElementById('fileSelectBtn');
    // 실제 파일 입력 필드 요소 가져오기
    const attachmentInput = document.getElementById('attachment');

    // 파일 선택 버튼에 클릭 이벤트 리스너 추가
    if (fileSelectButton) { // 요소가 존재하는지 확인 (불필요한 에러 방지)
        fileSelectButton.addEventListener('click', function() {
            // 커스텀 버튼 클릭 시 실제 파일 입력 필드 클릭 (숨겨진 필드를 여는 효과)
            attachmentInput.click();
        });
    }

    // 실제 파일 입력 필드에 변경(change) 이벤트 리스너 추가
    if (attachmentInput) { // 요소가 존재하는지 확인
        attachmentInput.addEventListener('change', function() {
            // 파일이 선택되면 updateFileName 함수를 호출하여 파일 이름을 업데이트
            updateFileName(this); // 'this'는 change 이벤트를 발생시킨 attachmentInput을 참조
        });
    }
});