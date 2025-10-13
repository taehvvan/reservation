<template>
  <div class="page-container">
    <div v-if="inquiry" class="detail-wrapper">
      <h2 class="page-title">문의 상세</h2>
      
      <div class="detail-section">
        <h3>{{ inquiry.title }}</h3>
        <div class="info-bar">
          <span><strong>작성자:</strong> {{ inquiry.authorName }} ({{ inquiry.isMember ? '회원' : '비회원' }})</span>
          <span v-if="inquiry.isMember"><strong>이메일:</strong> {{ inquiry.userEmail }}</span>
          <span v-else><strong>연락처:</strong> {{ inquiry.phoneNumber }}</span>
          <span><strong>작성일:</strong> {{ new Date(inquiry.createdAt).toLocaleString() }}</span>
        </div>
      </div>

      <div class="content-box question">
        <h4>문의 내용</h4>
        <p>{{ inquiry.content }}</p>
      </div>

      <div v-if="inquiry.status === 'ANSWERED'" class="content-box answer">
        <h4>답변 내용</h4>
        <p>{{ inquiry.answer }}</p>
        <p class="answer-date">답변일: {{ new Date(inquiry.answeredAt).toLocaleString() }}</p>
      </div>

      <form v-else @submit.prevent="submitAnswer" class="answer-form">
        <h4>답변 작성</h4>
        <textarea v-model="answer" rows="10" placeholder="답변을 입력하세요..."></textarea>
        <div class="form-actions">
          <button type="submit" class="btn btn-primary">답변 등록</button>
        </div>
      </form>

    </div>
    <div v-else class="loading-state">
      <p>문의 정보를 불러오는 중입니다...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const inquiryId = route.params.id;

const inquiry = ref(null);
const answer = ref('');

const fetchInquiryDetail = async () => {
  try {
    const response = await axios.get(`/api/inquiries/${inquiryId}`);
    inquiry.value = response.data;
  } catch (error) {
    console.error('문의 상세 정보를 불러오는 데 실패했습니다.', error);
    alert('문의 정보를 불러오는 데 실패했습니다.');
  }
};

const submitAnswer = async () => {
  if (!answer.value.trim()) {
    alert('답변 내용을 입력해주세요.');
    return;
  }
  try {
    await axios.post(`/api/inquiries/${inquiryId}/answer`, { answer: answer.value });
    alert('답변이 성공적으로 등록되었습니다.');
    fetchInquiryDetail(); // 데이터 새로고침
  } catch (error) {
    console.error('답변 등록에 실패했습니다.', error);
    alert('답변 등록에 실패했습니다.');
  }
};

onMounted(fetchInquiryDetail);
</script>

<style scoped>
.page-container {
  padding: 40px;
  background-color: #f9f9f9;
}
.detail-wrapper {
  max-width: 900px;
  margin: 0 auto;
  background-color: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}
.page-title {
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 10px;
  padding-bottom: 15px;
  border-bottom: 2px solid #eee;
}
.detail-section {
  margin-bottom: 30px;
}
.detail-section h3 {
  font-size: 1.5rem;
  margin-bottom: 15px;
}
.info-bar {
  display: flex;
  gap: 20px;
  font-size: 0.9rem;
  color: #666;
  flex-wrap: wrap;
}
.content-box {
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.content-box h4 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 10px 0;
}
.content-box p {
  white-space: pre-wrap;
  line-height: 1.7;
  margin: 0;
}
.question {
  background-color: #f5f8ff;
  border: 1px solid #e0e8f9;
}
.answer {
  background-color: #f3f3f3;
  border: 1px solid #e0e0e0;
}
.answer-date {
  text-align: right;
  font-size: 0.85rem;
  color: #888;
  margin-top: 15px !important;
}
.answer-form textarea {
  width: 100%;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  box-sizing: border-box;
  resize: vertical;
}
.answer-form textarea:focus {
  outline: none;
  border-color: #4A69A1;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}
.btn-primary {
  background-color: #4A69A1;
  color: white;
}
.loading-state {
  text-align: center;
  padding: 50px;
  color: #888;
}
</style>