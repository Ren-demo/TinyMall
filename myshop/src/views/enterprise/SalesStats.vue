<template>
  <div class="sales-stats-section">
    <div class="section-header">
      <h2>销售统计</h2>
      <div class="date-range">
        <select v-model="selectedPeriod" class="period-select">
          <option value="week">本周</option>
          <option value="month">本月</option>
          <option value="quarter">本季度</option>
          <option value="year">本年</option>
        </select>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <p>加载统计数据中...</p>
    </div>

    <div v-else class="stats-container">
      <div class="stats-grid">
        <div class="stat-card large">
          <div class="stat-icon">💰</div>
          <div class="stat-body">
            <span class="stat-label">总销售额</span>
            <div class="stat-value">¥{{ stats.totalSales }}</div>
            <span class="stat-trend positive">📈 +12.5%</span>
          </div>
        </div>

        <div class="stat-card large">
          <div class="stat-icon">📦</div>
          <div class="stat-body">
            <span class="stat-label">订单数量</span>
            <div class="stat-value">{{ stats.orderCount }}</div>
            <span class="stat-trend positive">📈 +8.3%</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">💵</div>
          <div class="stat-body">
            <span class="stat-label">平均单价</span>
            <div class="stat-value">¥{{ (parseFloat(stats.totalSales) / stats.orderCount).toFixed(2) }}</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">⭐</div>
          <div class="stat-body">
            <span class="stat-label">客户满意度</span>
            <div class="stat-value">{{ satisfaction }}%</div>
          </div>
        </div>
      </div>

      <div class="charts-grid">
        <div class="chart-card">
          <div class="chart-header">📈 销售趋势</div>
          <div class="chart-placeholder">
            <svg class="trend-chart" viewBox="0 0 300 150">
              <polyline
                points="10,100 50,80 90,90 130,50 170,70 210,40 250,60 290,30"
                fill="none"
                stroke="#28a745"
                stroke-width="3"
              />
              <polyline
                points="10,100 50,80 90,90 130,50 170,70 210,40 250,60 290,30"
                fill="none"
                stroke="#28a745"
                stroke-width="3"
                opacity="0.1"
              />
            </svg>
            <p class="chart-note">过去 7 天销售趋势（演示数据）</p>
          </div>
        </div>

        <div class="chart-card">
          <div class="chart-header">🎯 订单状态分布</div>
          <div class="status-breakdown">
            <div class="status-item">
              <div class="status-bar">
                <div class="status-fill primary" style="width: 35%"></div>
              </div>
              <span class="status-label">供货商已发货</span>
              <span class="status-count">35%</span>
            </div>
            <div class="status-item">
              <div class="status-bar">
                <div class="status-fill success" style="width: 45%"></div>
              </div>
              <span class="status-label">中间商已接受</span>
              <span class="status-count">45%</span>
            </div>
            <div class="status-item">
              <div class="status-bar">
                <div class="status-fill warning" style="width: 15%"></div>
              </div>
              <span class="status-label">已支付</span>
              <span class="status-count">15%</span>
            </div>
            <div class="status-item">
              <div class="status-bar">
                <div class="status-fill danger" style="width: 5%"></div>
              </div>
              <span class="status-label">拒绝</span>
              <span class="status-count">5%</span>
            </div>
          </div>
        </div>
      </div>

      <div class="summary-card">
        <div class="summary-header">📋 关键指标汇总</div>
        <div class="summary-table">
          <div class="summary-row">
            <span class="summary-key">总收入</span>
            <span class="summary-value primary">¥{{ stats.totalSales }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-key">订单总数</span>
            <span class="summary-value">{{ stats.orderCount }} 单</span>
          </div>
          <div class="summary-row">
            <span class="summary-key">转化率</span>
            <span class="summary-value success">{{ conversionRate }}%</span>
          </div>
          <div class="summary-row">
            <span class="summary-key">客户复购率</span>
            <span class="summary-value success">{{ repurchaseRate }}%</span>
          </div>
        </div>
      </div>

      <div class="action-buttons">
        <button class="btn btn-outline">📥 导出报告</button>
        <button class="btn btn-success">📧 发送到邮箱</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '../../services/api'

const stats = ref({ totalSales: '0.00', orderCount: 0 })
const loading = ref(true)
const selectedPeriod = ref('week')
const satisfaction = ref(0)
const conversionRate = ref(0)
const repurchaseRate = ref(0)

onMounted(async () => {
  stats.value = await api.fetchSalesStats()
  satisfaction.value = Math.floor(Math.random() * 15 + 85)
  conversionRate.value = Math.floor(Math.random() * 10 + 5)
  repurchaseRate.value = Math.floor(Math.random() * 30 + 20)
  loading.value = false
})
</script>

<style scoped>
.sales-stats-section {
  width: 100%;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.section-header h2 {
  margin: 0;
}

.period-select {
  padding: 0.75rem 1rem;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.period-select:focus {
  border-color: #28a745;
  outline: none;
  box-shadow: 0 0 0 3px rgba(40, 167, 69, 0.1);
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #6c757d;
}

.stats-container {
  display: grid;
  gap: 2rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.stat-card.large {
  grid-column: span 2;
}

.stat-icon {
  font-size: 2.5rem;
}

.stat-body {
  flex: 1;
}

.stat-label {
  display: block;
  font-size: 0.85rem;
  color: #6c757d;
  margin-bottom: 0.5rem;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
}

.stat-trend {
  display: block;
  font-size: 0.85rem;
  margin-top: 0.5rem;
}

.stat-trend.positive {
  color: #28a745;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 1.5rem;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.chart-header {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.chart-placeholder {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
}

.trend-chart {
  width: 100%;
  max-width: 300px;
  height: 150px;
}

.chart-note {
  font-size: 0.85rem;
  color: #6c757d;
  text-align: center;
  margin-top: 1rem;
}

.status-breakdown {
  display: grid;
  gap: 1rem;
}

.status-item {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 1rem;
  align-items: center;
}

.status-bar {
  background: #e9ecef;
  border-radius: 8px;
  height: 20px;
  overflow: hidden;
}

.status-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.status-fill.primary {
  background: #667eea;
}

.status-fill.success {
  background: #28a745;
}

.status-fill.warning {
  background: #ffc107;
}

.status-fill.danger {
  background: #dc3545;
}

.status-label {
  font-size: 0.9rem;
  color: #6c757d;
  min-width: 60px;
}

.status-count {
  font-weight: 600;
  color: #2c3e50;
  min-width: 40px;
  text-align: right;
}

.summary-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.summary-header {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
  border-bottom: 2px solid #e9ecef;
  padding-bottom: 0.75rem;
}

.summary-table {
  display: grid;
  gap: 0.75rem;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 6px;
}

.summary-key {
  font-weight: 600;
  color: #6c757d;
}

.summary-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: #2c3e50;
}

.summary-value.primary {
  color: #667eea;
}

.summary-value.success {
  color: #28a745;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.action-buttons .btn {
  flex: 0 1 auto;
}

@media (max-width: 768px) {
  .stat-card.large {
    grid-column: span 1;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .charts-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
    justify-content: stretch;
  }

  .action-buttons .btn {
    width: 100%;
  }
}
</style>