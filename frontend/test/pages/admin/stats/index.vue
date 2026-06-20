<template>
	<view class="stats-container">
		<view class="filter-box">
			<view class="segment-ctrl">
				<text :class="['seg-item', currentRange === 7 ? 'active' : '']" @click="changeRange(7)">近7天</text>
				<text :class="['seg-item', currentRange === 30 ? 'active' : '']" @click="changeRange(30)">近30天</text>
			</view>
			<view class="date-display">{{ startDate }} 至 {{ endDate }}</view>
		</view>

		<view class="chart-card">
			<view class="card-title">访客时间趋势</view>
			<view class="charts-box">
				<qiun-data-charts 
				  type="line"
				  :opts="lineOpts"
				  :chartData="trendChartData"
				  :canvas2d="true"
				/>
			</view>
		</view>

		<view class="chart-card">
			<view class="card-title">来访单位占比 (Top10)</view>
			<view class="charts-box" v-if="companyChartData.series[0].data.length > 0">
				<qiun-data-charts 
				  type="pie"
				  :opts="pieOpts"
				  :chartData="companyChartData"
				  :canvas2d="true"
				/>
			</view>
			<view class="empty-state" v-else>暂无单位数据</view>
		</view>

		<view class="chart-card" v-if="userRole === 'admin'">
			<view class="card-title">被访部门排行 (Top10)</view>
			<view class="charts-box" v-if="deptChartData.categories.length > 0">
				<qiun-data-charts 
				  type="column"
				  :opts="colOpts"
				  :chartData="deptChartData"
				  :canvas2d="true"
				/>
			</view>
			<view class="empty-state" v-else>暂无部门数据</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getDashboardCharts } from '@/api/admin.js';

const userRole = ref(uni.getStorageSync('userRole') || 'host');
const currentRange = ref(7);
const startDate = ref('');
const endDate = ref('');

// 图表数据源
const trendChartData = ref({ categories: [], series: [] });
const companyChartData = ref({ series: [{ data: [] }] });
const deptChartData = ref({ categories: [], series: [] });

// uCharts 图表基础配置
const lineOpts = ref({
	color: ["#1890FF"],
	padding: [15, 10, 0, 15],
	enableScroll: false,
	legend: { show: false },
	xAxis: { disableGrid: true },
	yAxis: { gridType: "dash", dashLength: 2, min: 0 }
});

const pieOpts = ref({
	color: ["#1890FF", "#91CB74", "#FAC858", "#EE6666", "#73C0DE", "#3CA272", "#FC8452", "#9A60B4", "#ea7ccc"],
	padding: [5, 5, 5, 5],
	enableScroll: false
});

const colOpts = ref({
	color: ["#91CB74"],
	padding: [15, 15, 0, 5],
	enableScroll: false,
	legend: { show: false },
	xAxis: { disableGrid: true, labelCount: 5 },
	yAxis: { min: 0 }
});

onMounted(() => {
	changeRange(7);
});

// 切换时间范围
const changeRange = (days) => {
	currentRange.value = days;
	const end = new Date();
	const start = new Date();
	start.setDate(end.getDate() - days + 1); // 包含今天

	endDate.value = formatDate(end);
	startDate.value = formatDate(start);

	fetchChartData();
};

// 工具函数：格式化日期 yyyy-MM-dd
const formatDate = (date) => {
	const y = date.getFullYear();
	const m = String(date.getMonth() + 1).padStart(2, '0');
	const d = String(date.getDate()).padStart(2, '0');
	return `${y}-${m}-${d}`;
};

// 获取图表数据
const fetchChartData = async () => {
	uni.showLoading({ title: '加载中' });
	try {
		const res = await getDashboardCharts({
			startDate: startDate.value,
			endDate: endDate.value
		});
		
		console.log("前端接收到的看板数据:", res); 
		
		// 兼容不同的请求封装方案
		const responseData = res.code === '00000' ? res.data : (res.data && res.data.code === '00000' ? res.data.data : null);

		if (responseData) {
			formatTrendData(responseData.trendStats || []);
			formatCompanyData(responseData.companyStats || []);
			if (userRole.value === 'admin') {
				formatDeptData(responseData.deptStats || []);
			}
		}
	} catch (error) {
		console.error("图表数据获取失败", error);
		uni.showToast({ title: '获取数据失败', icon: 'none' });
	} finally {
		uni.hideLoading();
	}
};

// 格式化折线图 (趋势)
const formatTrendData = (dataList) => {
	const categories = [];
	const seriesData = [];
	dataList.forEach(item => {
		const dateStr = item.name || '';
		categories.push(dateStr.length >= 10 ? dateStr.substring(5, 10) : dateStr); 
		seriesData.push(item.value);
	});
	
	// 【关键修复】Vue3 下给 uCharts 赋值必须深拷贝
	trendChartData.value = JSON.parse(JSON.stringify({
		categories: categories,
		series: [{ name: "访客数", data: seriesData }]
	}));
};

// 格式化饼图 (来访单位)
const formatCompanyData = (dataList) => {
	const mappedData = dataList.map(item => ({
		name: item.name,
		value: item.value
	}));
	
	// 【关键修复】Vue3 下给 uCharts 赋值必须深拷贝
	companyChartData.value = JSON.parse(JSON.stringify({
		series: [{ data: mappedData }]
	}));
};

// 格式化柱状图 (部门排行)
const formatDeptData = (dataList) => {
	const categories = [];
	const seriesData = [];
	dataList.forEach(item => {
		categories.push(item.name);
		seriesData.push(item.value);
	});
	
	// 【关键修复】Vue3 下给 uCharts 赋值必须深拷贝
	deptChartData.value = JSON.parse(JSON.stringify({
		categories: categories,
		series: [{ name: "接待数", data: seriesData }]
	}));
};
</script>

<style scoped>
.stats-container {
	min-height: 100vh;
	background-color: #f5f7fa;
	padding: 20rpx;
}

.filter-box {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
	background: #fff;
	padding: 20rpx 30rpx;
	border-radius: 16rpx;
}

.segment-ctrl {
	display: flex;
	background: #f0f2f5;
	border-radius: 8rpx;
	overflow: hidden;
}

.seg-item {
	padding: 10rpx 30rpx;
	font-size: 26rpx;
	color: #666;
}

.seg-item.active {
	background: #007aff;
	color: #fff;
}

.date-display {
	font-size: 24rpx;
	color: #999;
}

.chart-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.card-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
	border-left: 8rpx solid #007aff;
	padding-left: 16rpx;
}

.charts-box {
	width: 100%;
	height: 450rpx;
}

.empty-state {
	height: 300rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #ccc;
	font-size: 28rpx;
}
</style>