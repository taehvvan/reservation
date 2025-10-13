<template>
    <div class="chart-flex-wrapper">
      <h3>월별 총 매출 추이 및 예약 건수</h3>
      <div class="filter-buttons">
        <button
          :class="{ active: selectedPeriod === 'week' }"
          @click="setPeriod('week')"
        >
          주별
        </button>
        <button
          :class="{ active: selectedPeriod === 'month' }"
          @click="setPeriod('month')"
        >
          월별
        </button>
        <button
          :class="{ active: selectedPeriod === 'year' }"
          @click="setPeriod('year')"
        >
          연도별
        </button>
      </div>
      <div class="chart-render-area">
        <Line
          id="monthly-sales-chart"
          :options="chartOptions"
          :data="chartData"
        />
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { Line } from 'vue-chartjs'; 
  import axios from 'axios';
  import { 
      Chart as ChartJS, 
      Title, Tooltip, Legend, 
      LineElement, PointElement, BarElement, 
      CategoryScale, LinearScale
  } from 'chart.js';
  
  // Chart.js 모듈 등록
  ChartJS.register(
      Title, Tooltip, Legend, 
      LineElement, PointElement, BarElement, 
      CategoryScale, LinearScale
  );
  
  // 기간 상태 정의 (기본값: month)
  const selectedPeriod = ref('month'); 
  
  const chartData = ref({
      labels: [],
      datasets: []
  });
  
  const chartOptions = {
      barPercentage: 0.95, 
      categoryPercentage: 0.9, 
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
          mode: 'index', 
          intersect: false,
      },
      scales: {
          // Y축 (매출) - 동적 범위 지원을 위해 max/stepSize 제거
          price: { 
              type: 'linear',
              display: true,
              position: 'left',
              title: { 
                  display: true,
                  text: '총 매출액 (원)'
              },
              min: 0,
              grid: {
                  drawOnChartArea: false,
              }
          },
          // Y축 (건수) - 동적 범위 지원을 위해 max/stepSize 제거
          count: {
              type: 'linear',
              display: true,
              position: 'right',
              title: {
                  display: true,
                  text: '예약 건수 (건)'
              },
              min: 0,
          },
          // X축 설정 유지
          x: { 
              padding: {
                  left: 0,
                  right: 0
              },
              offset: true, 
              title: { 
                  display: true,
                  text: '기간 (월)'
              }
          }
      }
  };
  
  // 기간 설정 및 데이터 호출 함수
  const setPeriod = (period) => {
      if (selectedPeriod.value !== period) {
          selectedPeriod.value = period;
          fetchData(); // 기간이 변경되면 데이터를 다시 가져옵니다.
      }
  };
  
  const fetchData = async () => {
      try {
          const token = localStorage.getItem('accessToken'); 
          
          // API URL에 period 쿼리 파라미터 추가
          const apiUrl = `${import.meta.env.VITE_APP_API_URL}/api/admin/trend/trend?period=${selectedPeriod.value}`;
  
          const response = await axios.get(apiUrl, {
              headers: {
                  'Authorization': token ? `Bearer ${token}` : '' 
              }
          }); 
          const data = response.data;
  
          const labels = data.map(item => item.period);
          const salesData = data.map(item => item.totalSales);
          const countData = data.map(item => item.totalCount);
  
          // X축 제목 업데이트
          const xTitle = selectedPeriod.value === 'week' ? '일' : 
                         selectedPeriod.value === 'month' ? '월' : '년';
          chartOptions.scales.x.title.text = `기간 (${xTitle})`;
  
  
          chartData.value = {
              labels: labels,
              
              datasets: [
                  {
                      type: 'bar',
                      label: '총 매출',
                      // 개선된 연한 파란색
                      backgroundColor: '#A9D1F7', 
                      data: salesData,
                      yAxisID: 'price',
                      order: 2 // 막대가 뒤에 깔림
                  },
                  {
                      type: 'line', 
                      label: '예약 건수',
                      // 개선된 선명한 녹색
                      borderColor: '#059669', 
                      fill: false, // 배경 채우기 제거
                      data: countData,
                      yAxisID: 'count',
                      order: 1, // 꺾은선이 위에 덮어짐
                      pointRadius: 5, 
                      pointHoverRadius: 7, 
                  },
              ]
          };
      } catch (error) {
          console.error('Error fetching data:', error);
      }
  };
  
  onMounted(fetchData);
  </script>
  
  <style scoped>
  /* 버튼 영역 스타일 */
  .filter-buttons {
      margin-bottom: 15px;
      display: flex;
      gap: 10px;
  }
  
  .filter-buttons button {
      padding: 6px 12px;
      border: 1px solid #ccc;
      border-radius: 4px;
      background-color: #f9f9f9;
      cursor: pointer;
      transition: background-color 0.2s, border-color 0.2s;
      font-size: 14px;
  }
  
  .filter-buttons button.active {
      /* 활성 버튼 스타일 */
      background-color: #3B82F6; 
      color: white;
      border-color: #3B82F6;
  }
  
  .filter-buttons button:hover:not(.active) {
      background-color: #eee;
  }
  
  /* 기존 스타일 유지 */
  .chart-flex-wrapper {
      height: 100%;
      display: flex;
      flex-direction: column;
  }
  
  .chart-render-area {
      flex-grow: 1; 
      position: relative; 
      max-height: 100%;
  }
  
  #monthly-sales-chart {
      max-width: 100%;
      max-height: 100%;
  }
  </style>
