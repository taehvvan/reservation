<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <header class="modal-header">
        <h2>쿠폰함</h2>
        <button class="btn-close" @click="$emit('close')">×</button>
      </header>
      <div class="modal-body">
        <p v-if="coupons.length === 0" class="no-coupons">사용 가능한 쿠폰이 없습니다.</p>
        <div v-else class="coupon-list">
          <div
            v-for="coupon in coupons"
            :key="coupon.id"
            class="coupon-item"
            @click="selectCoupon(coupon)"
          >
            <div class="coupon-discount">
              <strong>{{ formatDiscount(coupon) }}</strong>
            </div>
            <div class="coupon-info">
              <h4>{{ coupon.name }}</h4>
              <p>유효기간: {{ coupon.expiryDate }} 까지</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  coupons: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['close', 'select-coupon']);

const formatDiscount = (coupon) => {
  if (coupon.type === 'PERCENT') {
    return `${coupon.discount}%`;
  }
  if (coupon.type === 'FIXED') {
    return `${coupon.discount.toLocaleString()}원`;
  }
  return '';
};

const selectCoupon = (coupon) => {
  emit('select-coupon', coupon);
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-content {
  background-color: #fff;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 5px 25px rgba(0,0,0,0.2);
}
.modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
}
.btn-close {
  background: none;
  border: none;
  font-size: 2rem;
  cursor: pointer;
  color: #888;
}
.modal-body {
  padding: 25px;
  overflow-y: auto;
}
.no-coupons {
  text-align: center;
  color: #888;
  padding: 40px 0;
}
.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.coupon-item {
  display: flex;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  overflow: hidden;
}
.coupon-item:hover {
  border-color: #0A2A66;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.coupon-discount {
  background-color: #0A2A66;
  color: white;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  font-weight: 800;
  min-width: 120px;
  text-align: center;
}
.coupon-info {
  padding: 15px;
}
.coupon-info h4 {
  margin: 0 0 5px 0;
  font-size: 1.1rem;
}
.coupon-info p {
  margin: 0;
  font-size: 0.9rem;
  color: #777;
}
</style>