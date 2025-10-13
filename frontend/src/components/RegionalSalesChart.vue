<template>
  <div class="chart-flex-wrapper"> 
    
    <header class="chart-header">
      <h3>지역별 매출 분포</h3>
      <div class="filter-buttons">
        <button @click="fetchData('daily')" :class="{ active: period === 'daily' }">일별</button>
        <button @click="fetchData('monthly')" :class="{ active: period === 'monthly' }">월별</button>
        <button @click="fetchData('yearly')" :class="{ active: period === 'yearly' }">연도별</button>
      </div>
    </header>

    <div class="chart-render-area"> 
        <Doughnut
            id="regional-sales-chart"
            :options="chartOptions"
            :data="chartData"
        />
        </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Doughnut } from 'vue-chartjs';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import axios from 'axios';

ChartJS.register(ArcElement, Tooltip, Legend);

const period = ref('monthly');
const chartData = ref({
  labels: [],
  datasets: []
});

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false
};

const fetchData = async (selectedPeriod) => {
  period.value = selectedPeriod;
  try {
    const token = localStorage.getItem('accessToken'); 

    const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/admin/regional/${selectedPeriod}`, {
            headers: {
                // 토큰이 있다면 Authorization 헤더에 Bearer 접두사와 함께 추가
                'Authorization': token ? `Bearer ${token}` : '' 
            }
        }); 

    //const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/sales/regional/${selectedPeriod}`);
    const data = response.data;

    const labels = data.map(item => item.region);
    const salesData = data.map(item => item.totalSales);
    const backgroundColors = [
      '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'
    ];
    
    chartData.value = {
      labels: labels,
      datasets: [
        {
          backgroundColor: backgroundColors.slice(0, labels.length),
          data: salesData
        }
      ]
    };
  } catch (error) {
    console.error('Error fetching data:', error);
  }
};

onMounted(() => {
  fetchData(period.value);
});
</script>

<style scoped>
/* RegionalSalesChart.vue의 <style scoped> 블록 수정/추가 */

.chart-header {
    display: flex;
    justify-content: space-between; /* 🎯 제목을 왼쪽, 버튼을 오른쪽으로 정렬 */
    align-items: center; /* 수직 중앙 정렬 */
    margin-bottom: 15px; /* 차트와의 간격 확보 */
}

.filter-buttons {
    display: flex;
    border: 1px solid #ddd; /* 버튼 전체를 감싸는 테두리 */
    border-radius: 6px;
    overflow: hidden; /* 버튼 간 경계선을 깔끔하게 처리 */
}

.filter-buttons button {
    background-color: #fff;
    color: #4B5563; /* 기본 텍스트 색상 */
    border: none; /* 개별 버튼의 기본 테두리 제거 */
    padding: 8px 12px;
    font-size: 0.9rem;
    cursor: pointer;
    transition: all 0.2s;
    
    /* 버튼 사이에 세로 구분선 추가 */
    border-right: 1px solid #ddd;
}

.filter-buttons button:last-child {
    border-right: none; /* 마지막 버튼의 오른쪽 구분선 제거 */
}

.filter-buttons button.active {
    background-color: #4F46E5; /* 🎯 활성화된 버튼의 배경색 */
    color: white;
    font-weight: 600;
}

.filter-buttons button:hover:not(.active) {
    background-color: #f0f0f0; /* 호버 효과 */
}

/* 1. 🎯 전체 컴포넌트를 수직 Flex 컨테이너로 만듭니다. */
.chart-flex-wrapper {
    height: 100%; /* 부모 .chart-card의 높이를 상속 */
    display: flex;
    flex-direction: column; 
}

/* 2. 🎯 차트 렌더링 영역이 남은 공간을 모두 차지하도록 설정 */
.chart-render-area {
    flex-grow: 1; 
    position: relative; /* 캔버스 위치 지정을 위해 */
}

/* 3. 🎯 캔버스(Doughnut) 자체가 렌더링 영역을 채우도록 max-height 설정 유지 */
#regional-sales-chart {
    max-width: 100%;
    max-height: 100%;
}
</style>