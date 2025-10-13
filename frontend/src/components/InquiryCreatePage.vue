<template>
  <div class="page-container">
    <div class="form-wrapper">
      <h2 class="form-title">문의하기</h2>
      <p class="form-subtitle">궁금한 점이 있으시면 언제든지 문의해주세요.</p>
      <form @submit.prevent="submit">
        <div class="form-group">
          <label for="authorName">작성자명</label>
          <input
            type="text"
            id="authorName"
            class="form-input"
            v-model="inquiry.authorName"
            :disabled="authStore.isLoggedIn"
            required
          />
        </div>

        <div v-if="!authStore.isLoggedIn" class="form-group">
          <label for="contactEmail">이메일 (답변 알림용)</label>
          <input
            type="temailel"
            id="contactEmail"
            class="form-input"
            v-model="inquiry.contactEmail"
            placeholder="답변을 받으실 이메일 주소를 입력하세요"
            required
          />
        </div>

        <div class="form-group">
          <label for="title">제목</label>
          <input
            type="text"
            id="title"
            class="form-input"
            v-model="inquiry.title"
            required
          />
        </div>

        <div class="form-group">
          <label for="content">문의 내용</label>
          <textarea
            id="content"
            class="form-textarea"
            rows="10"
            v-model="inquiry.content"
            required
          ></textarea>
        </div>

        <div class="form-group checkbox-group">
          <input type="checkbox" id="isPrivate" v-model="inquiry.secret" />
          <label for="isPrivate">비공개로 문의하기 (체크 시 본인과 관리자만 볼 수 있습니다)</label>
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="cancel">취소</button>
          <button type="submit" class="btn btn-primary">제출하기</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import axios from 'axios';

const authStore = useAuthStore();
const router = useRouter();

const inquiry = ref({
  title: '',
  content: '',
  authorName: '',
  contactEmail: '',
  secret: false,

});

const fetchUserData = async () => {
  if (authStore.isLoggedIn) {
    try {
      const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/users/info`, {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
      });
      inquiry.value.authorName = response.data.name;
    } catch (error) {
      console.error('사용자 정보 가져오기 실패:', error);
    }
  }
};

onMounted(() => {
  fetchUserData();
});

const submit = async () => {
  if (!inquiry.value.authorName || !inquiry.value.title || !inquiry.value.content) {
    alert('모든 필수 항목을 입력해주세요.');
    return;
  }
  if (!authStore.isLoggedIn && !inquiry.value.contactEmail) {
    alert('답변을 받으실 이메일을 입력해주세요.');
    return;
  }

  try {
    // --- [수정] 로그인 상태에 따라 API URL과 헤더를 다르게 설정 ---
    let url = '';
    let config = {};

    if (authStore.isLoggedIn) {
      // 회원용 API
      url = '/api/inquiries/member';
      config.headers = {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      };
    } else {
      // 비회원용 API
      url = '/api/inquiries/non-member';
    }

    await axios.post(url, inquiry.value, config);
    // --- 수정 끝 ---

    alert('문의가 성공적으로 등록되었습니다.');
    if (authStore.isLoggedIn) {
      router.push({ path: '/mypage', query: { tab: 'inquiries' } });
    } else {
      router.push('/');
    }
  } catch (error) {
    console.error('문의 등록에 실패했습니다.', error);
    alert('문의 등록에 실패했습니다.');
  }
};

const cancel = () => {
  router.go(-1);
};
</script>

<style scoped>
.page-container {
  padding: 40px 20px;
  background-color: #f9f9f9;
  min-height: 80vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}
.form-wrapper {
  width: 100%;
  max-width: 800px;
  background-color: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}
.form-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 10px;
  text-align: center;
}
.form-subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 30px;
}
.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
}
.form-input,
.form-textarea {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  box-sizing: border-box;
  transition: border-color 0.2s;
}
.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #4A69A1;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
}
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}
.btn-primary {
  background-color: #4A69A1;
  color: white;
}
.btn-primary:hover {
  background-color: #3A5280;
}
.btn-secondary {
  background-color: #f0f0f0;
  color: #333;
}
.btn-secondary:hover {
  background-color: #e0e0e0;
}

/* [추가] 체크박스 스타일 */
.checkbox-group {
  display: flex;
  align-items: center;
  gap: 10px;
}
.checkbox-group input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}
.checkbox-group label {
  margin-bottom: 0;
  cursor: pointer;
  color: #555;
  font-weight: 400;
}
</style>