<template>
	<view class="record-container">
		<view class="info-bar">
			<text class="info-text">共查询到 {{ total }} 条访客记录</text>
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
				<picker class="f-picker" mode="selector" :range="deptOptions" range-key="label"
					@change="e => filterForm.hostDeptId = deptOptions[e.detail.value].value">
					<view class="picker-text">
						{{ getDeptNameById(filterForm.hostDeptId) || '全部部门' }}
					</view>
				</picker>
			</view>

			<view class="filter-actions">
				<button class="f-btn reset" @click="resetFilter">重置条件</button>
				<button class="f-btn search" @click="executeSearch">确定查询</button>
			</view>
		</view>

		<scroll-view scroll-y class="list-area" @scrolltolower="loadMore">
			<view v-if="recordList.length === 0" class="empty-box">
				<image src="/static/images/tabbar/enroll.png" class="empty-icon"></image>
				<text>{{ emptyTip }}</text>
			</view>

			<view class="card" v-for="item in recordList" :key="item.id" @click="navToDetail(item.id)">
				<view class="card-top">
					<text class="time">预约时间：{{ item.visitDate }} {{ item.visitTime }}</text>
					<text class="status-tag" :class="'status-' + item.applicationStatus">
						{{ getStatusText(item.applicationStatus, item.visitedPersonApprovalStatus, item.adminApprovalStatus) }}
					</text>
				</view>

				<view class="card-content">
					<view class="row">
						<text class="label">访客姓名：</text>
						<text class="value">{{ item.applicantName }} ({{ item.applicantPhone }})</text>
					</view>
					<view class="row">
						<text class="label">来访单位：</text>
						<text class="value">{{ item.visitorCompany || '无' }}</text>
					</view>
					<view class="row" v-if="userRole !== 'host'">
						<text class="label">被 访 人：</text>
						<text class="value highlight">{{ item.visitedPersonName || '未知' }}
							({{ item.deptName || '无部门' }})</text>
					</view>
					<view class="row">
						<text class="label">来访事由：</text>
						<text class="value">{{ item.visitPurpose || '无' }}</text>
					</view>

					<view class="guard-action-tip" v-if="userRole === 'guard' && item.applicationStatus === 0">
						<text>▶ 提示：等待该访客到达时进行核验</text>
					</view>
				</view>
			</view>

			<view v-if="recordList.length > 0 && recordList.length >= total" class="no-more">
				- 已经到底啦 -
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
	import {
		getVisitorPage,
		getHostVisitorPage,
		getAdminVisitorPage,
		getGuardVisitorPage
	} from '@/api/visitor.js';
	import {
		getDeptOptions
	} from '@/api/employee.js';

	const userRole = ref('');
	const showFilter = ref(false);

	const recordList = ref([]);
	const pageNum = ref(1);
	const pageSize = ref(10);
	const total = ref(0);

	const filterForm = ref({
		startDate: '',
		endDate: '',
		company: '',
		hostDeptId: ''
	});

	const deptOptions = ref([]);

	onShow(async () => {
		userRole.value = uni.getStorageSync('userRole') || 'host';

		let pageTitle = '访客来访记录';
		if (userRole.value === 'host') pageTitle = '我的被访记录';
		if (userRole.value === 'guard') pageTitle = '今日待访名单';
		uni.setNavigationBarTitle({
			title: pageTitle
		});

		if (userRole.value === 'admin') {
			loadDept();
		}
		resetAndFetchData();
	});

	// 💡 新增：递归扁平化部门树结构函数
	const flattenDeptTree = (treeData, depth = 0) => {
		let result = [];
		treeData.forEach(node => {
			// 根据层级添加前缀空格和符号，实现视觉上的树状下拉列表
			const prefix = depth > 0 ? ' '.repeat(depth) + '├─ ' : '';
			result.push({
				label: prefix + node.label, // 选择器里显示的带缩进的名字
				value: node.value, // 传给后端的真实 ID
				realName: node.label // 选完之后回显在页面上的干净名字
			});
			// 如果有子部门，递归往下解析
			if (node.children && node.children.length > 0) {
				result = result.concat(flattenDeptTree(node.children, depth + 1));
			}
		});
		return result;
	};

	const loadDept = async () => {
		try {
			const res = await getDeptOptions();
			if (res.code === '00000' && res.data) {
				// 💡 修改：调用扁平化函数处理后端返回的数据
				const flatData = flattenDeptTree(res.data);
				deptOptions.value = [{
					label: '全部部门',
					value: '',
					realName: '全部部门'
				}, ...flatData];
			}
		} catch (error) {}
	};

	const getDeptNameById = (id) => {
		if (!id) return '';
		const target = deptOptions.value.find(item => item.value === id);
		// 💡 修改：回显时使用干净的 realName，而不是带 ├─ 符号的 label
		return target ? target.realName : '';
	};

	const resetFilter = () => {
		filterForm.value = {
			startDate: '',
			endDate: '',
			company: '',
			hostDeptId: ''
		};
		executeSearch();
	};

	const executeSearch = () => {
		showFilter.value = false;
		resetAndFetchData();
	};

	const resetAndFetchData = () => {
		pageNum.value = 1;
		recordList.value = [];
		total.value = 0;
		fetchRecordList();
	};

	const fetchRecordList = async () => {
		uni.showLoading({
			title: '加载中...'
		});
		try {
			let queryParams = {
				pageNum: pageNum.value,
				pageSize: pageSize.value,
				startDate: filterForm.value.startDate || undefined,
				endDate: filterForm.value.endDate || undefined,
				visitorCompany: filterForm.value.company || undefined,
				deptId: filterForm.value.hostDeptId || undefined
			};

			let res;
			if (userRole.value === 'host') {
				res = await getHostVisitorPage(queryParams);
			} else if (userRole.value === 'guard') {
				res = await getGuardVisitorPage(queryParams);
			} else if (userRole.value === 'admin') {
				res = await getAdminVisitorPage(queryParams);
			} else {
				res = await getVisitorPage(queryParams);
			}

			if (res.code === '00000' && res.data) {
				const newList = res.data.list || [];
				if (pageNum.value === 1) {
					recordList.value = newList;
				} else {
					recordList.value = recordList.value.concat(newList);
				}
				total.value = res.data.total || 0;
			} else {
				uni.showToast({
					title: res.msg || '获取数据失败',
					icon: 'none'
				});
			}
		} catch (error) {
			console.error('拉取记录异常:', error);
			uni.showToast({
				title: '网络请求异常',
				icon: 'none'
			});
		} finally {
			uni.hideLoading();
		}
	};

	const loadMore = () => {
		if (recordList.value.length >= total.value) return;
		pageNum.value++;
		fetchRecordList();
	};

	const getStatusText = (status, hostStatus, adminStatus) => {
		if (hostStatus === 2 || adminStatus === 2 || status === 2) return '已拒绝/失效';
		if (status === 1) return '已完成到访';
		if (hostStatus === 1 && adminStatus === 1 && status === 0) return '待来访(已全审)';
		return '审批中';
	};

	const emptyTip = computed(() => {
		if (userRole.value === 'guard') return '今日暂无双重审核通过的待到访访客';
		return '未查询到符合条件的记录';
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

	.status-1 {
		background-color: #f6ffed;
		color: #52c41a;
	}

	.status-2 {
		background-color: #fff1f0;
		color: #f5222d;
	}

	.status-0 {
		background-color: #fff7e6;
		color: #fa8c16;
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

	.no-more {
		text-align: center;
		font-size: 24rpx;
		color: #ccc;
		padding: 20rpx 0;
	}
</style>