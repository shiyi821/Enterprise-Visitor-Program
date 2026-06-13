<template>
	<view class="record-container">
		<view class="info-bar">
			<text class="info-text">共查询到 {{ filteredList.length }} 条访客记录</text>
			<view class="filter-toggle" @click="showFilter = !showFilter">
				<text>高级筛选统计</text>
				<text class="toggle-icon">{{ showFilter ? '▲' : '▼' }}</text>
			</view>
		</view>

		<view class="filter-panel" v-if="showFilter">
			<view class="filter-row">
				<text class="f-label">时间区间：</text>
				<view class="date-range">
					<picker mode="date" @change="e => filterForm.startDate = e.detail.value">
						<view class="date-box">{{ filterForm.startDate || '开始日期' }}</view>
					</picker>
					<text class="to">至</text>
					<picker mode="date" @change="e => filterForm.endDate = e.detail.value">
						<view class="date-box">{{ filterForm.endDate || '结束日期' }}</view>
					</picker>
				</view>
			</view>

			<view class="filter-row">
				<text class="f-label">来访单位：</text>
				<input class="f-input" v-model="filterForm.company" placeholder="输入访客所属单位模糊查询" />
			</view>

			<view class="filter-row" v-if="userRole === 'admin'">
				<text class="f-label">被访部门：</text>
				<picker class="f-picker" mode="selector" :range="deptOptions"
					@change="e => filterForm.hostDept = deptOptions[e.detail.value]">
					<view class="picker-text">{{ filterForm.hostDept || '全部部门' }}</view>
				</picker>
			</view>

			<view class="filter-actions">
				<button class="f-btn reset" @click="resetFilter">重置条件</button>
				<button class="f-btn search" @click="showFilter = false">确定查询</button>
			</view>
		</view>

		<scroll-view scroll-y class="list-area">
			<view v-if="filteredList.length === 0" class="empty-box">
				<image src="/static/images/tabbar/enroll.png" class="empty-icon"></image>
				<text>{{ emptyTip }}</text>
			</view>

			<view class="card" v-for="item in filteredList" :key="item.id" @click="navToDetail(item.id)">
				<view class="card-top">
					<text class="time">预约时间：{{ item.visitDate }} {{ item.visitTime }}</text>
					<text class="status-tag" :class="'status-' + item.status">
						{{ getStatusText(item.status) }}
					</text>
				</view>

				<view class="card-content">
					<view class="row"><text class="label">访客姓名：</text><text class="value">{{ item.visitorName }}
							({{ item.phone }})</text></view>
					<view class="row"><text class="label">来访单位：</text><text class="value">{{ item.company }}</text>
					</view>
					<view class="row" v-if="userRole !== 'host'">
						<text class="label">被 访 人：</text>
						<text class="value highlight">{{ item.hostName }} ({{ item.hostDept }})</text>
					</view>
					<view class="row"><text class="label">来访事由：</text><text class="value">{{ item.reason }}</text>
					</view>

					<view class="guard-action-tip" v-if="userRole === 'guard' && item.status === 3">
						<text>▶ 提示：等待该访客到达时扫码放行</text>
					</view>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue';
	import {
		onShow
	} from '@dcloudio/uni-app';

	const userRole = ref('');
	const currentHostName = '李总';
	const showFilter = ref(false); // 控制筛选面板开关

	// 筛选条件表单
	const filterForm = ref({
		startDate: '',
		endDate: '',
		company: '',
		hostDept: ''
	});

	const deptOptions = ref(['全部部门', '集团总部', '技术部', '市场部']);

	const getTodayDate = () => {
		const d = new Date();
		const year = d.getFullYear();
		const month = String(d.getMonth() + 1).padStart(2, '0');
		const day = String(d.getDate()).padStart(2, '0');
		return `${year}-${month}-${day}`;
	};
	const todayStr = getTodayDate();

	onShow(() => {
		userRole.value = uni.getStorageSync('userRole') || 'host';

		let pageTitle = '访客来访记录';
		if (userRole.value === 'host') pageTitle = '我的被访记录';
		if (userRole.value === 'guard') pageTitle = '今日到访人员名单';
		uni.setNavigationBarTitle({
			title: pageTitle
		});
	});

	const resetFilter = () => {
		filterForm.value = {
			startDate: '',
			endDate: '',
			company: '',
			hostDept: ''
		};
	};

	const emptyTip = computed(() => {
		if (userRole.value === 'guard') return '今日暂无已通过待到访的访客';
		return '未查询到符合条件的记录';
	});

	// 模拟系统后台全量库数据
	const allBackendRecords = ref([{
			id: 1,
			visitorName: '张三',
			phone: '13800138001',
			company: '腾讯科技',
			hostName: '李总',
			hostDept: '技术部',
			reason: '项目洽谈',
			visitDate: todayStr,
			visitTime: '14:00',
			status: 3
		},
		{
			id: 2,
			visitorName: '王五',
			phone: '13900139002',
			company: '阿里巴巴',
			hostName: '赵总',
			hostDept: '市场部',
			reason: '市场合作',
			visitDate: todayStr,
			visitTime: '10:00',
			status: 4
		},
		{
			id: 3,
			visitorName: '李四',
			phone: '13700137003',
			company: '字节跳动',
			hostName: '李总',
			hostDept: '技术部',
			reason: '面试',
			visitDate: '2026-05-15',
			visitTime: '09:30',
			status: 1
		},
		{
			id: 4,
			visitorName: '赵六',
			phone: '13600136004',
			company: '小微企业',
			hostName: '李总',
			hostDept: '技术部',
			reason: '送文件',
			visitDate: '2026-06-01',
			visitTime: '16:00',
			status: 4
		},
		{
			id: 5,
			visitorName: '孙七',
			phone: '13500135005',
			company: '推销公司',
			hostName: '李总',
			hostDept: '技术部',
			reason: '推销产品',
			visitDate: '2026-06-10',
			visitTime: '11:00',
			status: 2
		}
	]);

	const getStatusText = (status) => {
		const map = {
			0: '待审核',
			1: '审批中',
			2: '被访人拒绝',
			3: '待到访',
			4: '已到访完成'
		};
		return map[status] || '未知';
	};

	// 核心过滤与统计查询逻辑
	const filteredList = computed(() => {
		let list = allBackendRecords.value;

		// 1. 基础权限过滤
		if (userRole.value === 'host') {
			list = list.filter(item => item.hostName === currentHostName);
		} else if (userRole.value === 'guard') {
			// 门岗强制只能看今天且通过的，不参与高级筛选
			return list.filter(item => item.visitDate === todayStr && (item.status === 3 || item.status === 4));
		}

		// 2. 高级筛选 - 时间区间过滤
		if (filterForm.value.startDate) {
			list = list.filter(item => new Date(item.visitDate) >= new Date(filterForm.value.startDate));
		}
		if (filterForm.value.endDate) {
			list = list.filter(item => new Date(item.visitDate) <= new Date(filterForm.value.endDate));
		}

		// 3. 高级筛选 - 来访单位模糊查询
		if (filterForm.value.company) {
			list = list.filter(item => item.company.includes(filterForm.value.company));
		}

		// 4. 高级筛选 - 员工部门查询 (仅管理员有效)
		if (userRole.value === 'admin' && filterForm.value.hostDept && filterForm.value.hostDept !== '全部部门') {
			list = list.filter(item => item.hostDept === filterForm.value.hostDept);
		}

		return list.sort((a, b) => new Date(b.visitDate) - new Date(a.visitDate));
	});

	const navToDetail = (id) => {
		uni.navigateTo({
			url: `/pages/admin/detail/detail?id=${id}`
		});
	};
</script>

<style scoped>
	.record-container {
		display: flex;
		flex-direction: column;
		height: 100vh;
		background-color: #f5f7fa;
	}

	/* 顶部统计条与筛选按钮 */
	.info-bar {
		display: flex;
		justify-content: space-between;
		align-items: center;
		background-color: #e6f7ff;
		padding: 24rpx 30rpx;
		border-bottom: 1rpx solid #91d5ff;
	}

	.info-text {
		font-size: 26rpx;
		color: #0050b3;
		font-weight: bold;
	}

	.filter-toggle {
		font-size: 26rpx;
		color: #1890ff;
		display: flex;
		align-items: center;
	}

	.toggle-icon {
		margin-left: 8rpx;
		font-size: 20rpx;
	}

	/* 高级筛选面板 */
	.filter-panel {
		background-color: #fff;
		padding: 30rpx;
		border-bottom: 1rpx solid #eee;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	}

	.filter-row {
		display: flex;
		align-items: center;
		margin-bottom: 24rpx;
	}

	.f-label {
		width: 150rpx;
		font-size: 26rpx;
		color: #666;
	}

	.date-range {
		flex: 1;
		display: flex;
		align-items: center;
	}

	.date-box {
		border: 1px solid #ddd;
		padding: 10rpx 20rpx;
		border-radius: 8rpx;
		font-size: 24rpx;
		color: #333;
		width: 160rpx;
		text-align: center;
	}

	.to {
		margin: 0 16rpx;
		font-size: 24rpx;
		color: #999;
	}

	.f-input {
		flex: 1;
		border: 1px solid #ddd;
		padding: 10rpx 20rpx;
		border-radius: 8rpx;
		font-size: 24rpx;
	}

	.f-picker {
		flex: 1;
		border: 1px solid #ddd;
		padding: 10rpx 20rpx;
		border-radius: 8rpx;
	}

	.picker-text {
		font-size: 24rpx;
		color: #333;
	}

	.filter-actions {
		display: flex;
		justify-content: flex-end;
		margin-top: 30rpx;
		gap: 20rpx;
	}

	.f-btn {
		margin: 0;
		padding: 0 40rpx;
		height: 60rpx;
		line-height: 60rpx;
		font-size: 26rpx;
		border-radius: 30rpx;
	}

	.reset {
		background-color: #f5f5f5;
		color: #666;
	}

	.search {
		background-color: #007aff;
		color: #fff;
	}

	.list-area {
		flex: 1;
		padding: 20rpx;
		box-sizing: border-box;
	}

	.empty-box {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		margin-top: 150rpx;
		color: #999;
		font-size: 28rpx;
	}

	.empty-icon {
		width: 120rpx;
		height: 120rpx;
		margin-bottom: 20rpx;
		opacity: 0.25;
	}

	.card {
		background-color: #ffffff;
		border-radius: 16rpx;
		padding: 30rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.02);
	}

	.card-top {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
		padding-bottom: 20rpx;
		border-bottom: 1rpx dashed #eee;
	}

	.time {
		font-size: 28rpx;
		color: #333;
		font-weight: bold;
	}

	.status-tag {
		font-size: 22rpx;
		padding: 4rpx 16rpx;
		border-radius: 8rpx;
		font-weight: bold;
	}

	.status-0,
	.status-1 {
		background-color: #fff7e6;
		color: #fa8c16;
	}

	.status-2 {
		background-color: #fff1f0;
		color: #f5222d;
	}

	.status-3 {
		background-color: #e6f7ff;
		color: #1890ff;
	}

	.status-4 {
		background-color: #f6ffed;
		color: #52c41a;
	}

	.card-content .row {
		display: flex;
		margin-bottom: 14rpx;
		font-size: 26rpx;
		line-height: 1.5;
	}

	.card-content .label {
		color: #999;
		width: 140rpx;
		flex-shrink: 0;
	}

	.card-content .value {
		color: #333;
		flex: 1;
	}

	.card-content .highlight {
		color: #007aff;
		font-weight: bold;
	}

	.guard-action-tip {
		margin-top: 20rpx;
		padding: 14rpx;
		background-color: #fff7e6;
		border-radius: 8rpx;
		font-size: 24rpx;
		color: #d46b08;
		text-align: center;
		font-weight: 500;
	}
</style>