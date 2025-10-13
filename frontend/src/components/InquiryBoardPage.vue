<template>
  <div class="page-container">
    <div class="board-wrapper">
      <h2 class="page-title">문의 게시판</h2>
      
      <div v-if="selectedInquiry" class="detail-view">
        <button class="btn-back" @click="selectedInquiry = null">← 목록으로</button>
        <div class="detail-header">
          <h3>{{ selectedInquiry.title }}</h3>
          <div class="info-bar">
            <span><strong>작성자:</strong> {{ maskAuthorName(selectedInquiry.authorName) }}</span>
            <span><strong>작성일:</strong> {{ new Date(selectedInquiry.createdAt).toLocaleString() }}</span>
          </div>
        </div>
        <div class="content-box question">
          <h4>문의 내용</h4>
          <p>{{ selectedInquiry.content }}</p>
        </div>
        <div v-if="selectedInquiry.status === 'ANSWERED'" class="content-box answer">
          <h4>답변 내용</h4>
          <p>{{ selectedInquiry.answer }}</p>
        </div>
        <div v-else class="content-box answer-pending">
          <p>관리자의 답변을 기다리고 있습니다.</p>
        </div>
      </div>

      <table v-else class="inquiry-table">
        <thead>
          <tr>
            <th class="col-id">번호</th>
            <th class="col-status">상태</th>
            <th class="col-title">제목</th>
            <th class="col-author">작성자</th>
            <th class="col-date">작성일</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="inquiry in inquiries" :key="inquiry.id" @click="selectInquiry(inquiry)">
            <td>{{ inquiry.id }}</td>
            <td>
              <span class="status-badge" :class="inquiry.status.toLowerCase()">
                {{ inquiry.status === 'ANSWERED' ? '답변완료' : '대기중' }}
              </span>
            </td>
            <td class="title-cell">
              <span class="lock-icon" v-if="inquiry.secret">🔒</span>
              {{ inquiry.title }}
            </td>
            <td>{{ maskAuthorName(inquiry.authorName) }}</td>
            <td>{{ new Date(inquiry.createdAt).toLocaleDateString() }}</td>
          </tr>
          <tr v-if="inquiries.length === 0">
            <td colspan="5" class="empty-row">공개된 문의가 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const inquiries = ref([]);
const selectedInquiry = ref(null);


onMounted(async () => {
  try {
    const response = await axios.get('/api/inquiries/board');
    inquiries.value = response.data;
  } catch (error) {
    console.error('공개 문의 목록을 불러오는 데 실패했습니다.', error);
  }
});

// 비공개 글은 상세보기를 막고, 공개글만 선택 가능하도록 처리
const selectInquiry = (inquiry) => {
  if (inquiry.secret) {
    return;
  } else {
    selectedInquiry.value = inquiry;
  }
};

// 작성자 이름 마스킹 처리 (예: 홍길동 -> 홍*동)
const maskAuthorName = (name) => {
  if (name && name.length > 2) {
    return name.substring(0, 1) + '*' + name.substring(name.length - 1);
  }
  return name;
};
</script>

<style scoped>
.page-container {
  padding: 40px;
  background-color: #f9f9f9;
}
.board-wrapper {
  max-width: 1000px;
  margin: 0 auto;
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}
.page-title {
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 20px;
}
.inquiry-table {
  width: 100%;
  border-collapse: collapse;
  text-align: center;
}
.inquiry-table th, .inquiry-table td {
  padding: 15px;
  border-bottom: 1px solid #eee;
}
.inquiry-table th {
  background-color: #f5f5f5;
  font-weight: 600;
  color: #333;
}
.inquiry-table tbody tr {
  cursor: pointer;
  transition: background-color 0.2s;
}
.inquiry-table tbody tr:hover {
  background-color: #f9f9f9;
}
.col-id { width: 8%; }
.col-status { width: 12%; }
.col-title { width: 45%; text-align: left; }
.col-author { width: 15%; }
.col-date { width: 20%; }

.title-cell {
  display: flex;
  align-items: center;
}
.lock-icon {
  margin-right: 8px;
  color: #888;
}
.status-badge {
  padding: 5px 12px;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 700;
  color: white;
}
.status-badge.pending { background-color: #ff9800; }
.status-badge.answered { background-color: #4caf50; }
.empty-row {
  text-align: center;
  padding: 50px;
  color: #888;
}
/* 상세 보기 스타일 */
.btn-back {
  background: none;
  border: 1px solid #ccc;
  padding: 8px 15px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 20px;
}
.detail-header {
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}
.detail-header h3 {
  font-size: 1.6rem;
  margin-bottom: 10px;
}
.info-bar { display: flex; gap: 20px; font-size: 0.9rem; color: #666; }
.content-box { padding: 20px; border-radius: 8px; margin-bottom: 20px; }
.content-box h4 { margin: 0 0 10px 0; font-size: 1.1rem; }
.content-box p { white-space: pre-wrap; line-height: 1.7; margin: 0; }
.question { background-color: #f5f8ff; }
.answer { background-color: #f3f3f3; }
.answer-pending { text-align: center; padding: 40px; color: #888; }

/* [추가] 비공개 글에 대한 스타일 */
.secret-row {
  opacity: 0.6; /* 투명도 낮추기 */
  cursor: default; /* 마우스 커서 기본 모양으로 */
}
.secret-row:hover {
  background-color: inherit; /* hover 효과 제거 */
}
.status-badge.secret {
  background-color: #6c757d; /* 회색 배지 */
}
</style>