<template>
  <div class="page-container">
    <div class="table-wrapper">
      <h2 class="page-title">문의 관리</h2>
      <table class="inquiry-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>제목</th>
            <th>작성자</th>
            <th>상태</th>
            <th>작성일</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="inquiry in inquiries" :key="inquiry.id" @click="goToDetail(inquiry)">
            <td>{{ inquiry.id }}</td>
            <td>{{ inquiry.title }}</td>
            <td>{{ inquiry.authorName }}</td>
            <td>
              <span class="status-badge" :class="inquiry.status.toLowerCase()">
                {{ inquiry.status === 'ANSWERED' ? '답변완료' : '대기중' }}
              </span>
            </td>
            <td>{{ new Date(inquiry.createdAt).toLocaleString() }}</td>
          </tr>
          <tr v-if="inquiries.length === 0">
            <td colspan="5" class="empty-row">접수된 문의가 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const inquiries = ref([]);

onMounted(async () => {
  try {
    const response = await axios.get('/api/inquiries');
    inquiries.value = response.data;
  } catch (error) {
    console.error('문의 목록을 불러오는 데 실패했습니다.', error);
    alert('문의 목록을 불러오는 데 실패했습니다.');
  }
});

const goToDetail = (inquiry) => {
  router.push({ name: 'InquiryDetail', params: { id: inquiry.id } });
};
</script>

<style scoped>
.page-container {
  padding: 40px;
  background-color: #f9f9f9;
}
.table-wrapper {
  max-width: 1200px;
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
  text-align: left;
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
.status-badge {
  padding: 5px 12px;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 700;
  color: white;
}
.status-badge.pending {
  background-color: #ff9800;
}
.status-badge.answered {
  background-color: #4caf50;
}
.empty-row {
  text-align: center;
  padding: 50px;
  color: #888;
}
</style>