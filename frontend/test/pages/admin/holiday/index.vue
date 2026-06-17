<template>
	<view class="container">
		<view class="header-card">
			<view class="text-wrap">
				<text class="main-title">节假日/不可预约日期管理</text>
				<text class="desc">设置后，访客将无法选择这些日期进行预约申请</text>
			</view>

			<view class="action-panel">
				<view class="date-input-row">
					<view class="date-boxes">
						<input class="date-box year" type="number" maxlength="4" v-model="inputYear" placeholder="YYYY" @blur="padZero('year')" />
						<text class="separator">-</text>
						<input class="date-box month" type="number" maxlength="2" v-model="inputMonth" placeholder="MM" @blur="padZero('month')" />
						<text class="separator">-</text>
						<input class="date-box day" type="number" maxlength="2" v-model="inputDay" placeholder="DD" @blur="padZero('day')" />
					</view>
					<view class="action-btn-inline" @click="handleManualAdd">添加</view>
				</view>

				<picker mode="date" @change="handleAddHoliday">
					<view class="add-btn-full">📅 打开系统日历快捷选择</view>
				</picker>
			</view>
		</view>

		<scroll-view scroll-y class="list-area">
			<view v-if="sortedHolidays.length === 0" class="empty">暂无设置的休息日</view>

			<view v-for="(item, idx) in sortedHolidays" :key="idx" class="card">
				<view class="card-body">
					<view class="left-info">
						<text class="calendar-icon">🗓️</text>
						<text class="date-val">{{ item }}</text>
						<text class="status-tag status-off">休息日</text>
					</view>
					<text class="action-btn delete" @click="handleRemove(item)">移除</text>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
	import { ref, computed, onMounted } from 'vue';
	import { getHolidays, addHoliday, deleteHoliday } from '@/api/holiday.js';

	const holidays = ref([]);
	
	// 拆分成三个独立的响应式变量
	const inputYear = ref(new Date().getFullYear().toString()); // 默认填入今年
	const inputMonth = ref('');
	const inputDay = ref('');

	onMounted(() => {
		fetchHolidaysFromDB();
	});

	const fetchHolidaysFromDB = async () => {
		uni.showLoading({ title: '加载中...' });
		try {
			const res = await getHolidays();
			if (res.code === '00000' && Array.isArray(res.data)) {
				holidays.value = res.data;
			}
		} catch (error) {
			uni.showToast({ title: '获取假期失败', icon: 'none' });
		} finally {
			uni.hideLoading();
		}
	};

	const sortedHolidays = computed(() => {
		return [...holidays.value].sort((a, b) => new Date(a) - new Date(b));
	});

	// 失焦时自动补齐 0（比如输 5，离开变 05）
	const padZero = (type) => {
		if (type === 'month' && inputMonth.value.length === 1) {
			inputMonth.value = '0' + inputMonth.value;
		}
		if (type === 'day' && inputDay.value.length === 1) {
			inputDay.value = '0' + inputDay.value;
		}
	};

	// 执行后端添加请求
	const executeAdd = async (selectDate) => {
		if (holidays.value.includes(selectDate)) {
			return uni.showToast({ title: '该日期已存在', icon: 'none' });
		}
		
		uni.showLoading({ title: '保存中...' });
		try {
			const res = await addHoliday(selectDate);
			if (res.code === '00000') {
				holidays.value.push(selectDate);
				// 成功后只清空月日，保留年份方便连续添加
				inputMonth.value = '';
				inputDay.value = '';
				uni.showToast({ title: '添加成功', icon: 'success' });
			} else {
				uni.showToast({ title: res.msg || '添加失败', icon: 'none' });
			}
		} catch (error) {
			uni.showToast({ title: '网络异常', icon: 'none' });
		} finally {
			uni.hideLoading();
		}
	};

	// 方式一：三格输入拼接添加
	const handleManualAdd = () => {
		// 强制触发一次补 0
		padZero('month');
		padZero('day');
		
		const y = inputYear.value.trim();
		const m = inputMonth.value.trim();
		const d = inputDay.value.trim();

		if (!y || !m || !d || y.length !== 4) {
			return uni.showToast({ title: '请将年月日填写完整', icon: 'none' });
		}

		const dateStr = `${y}-${m}-${d}`;

		// 真实性校验（防止 02-30）
		const checkDate = new Date(dateStr);
		if (isNaN(checkDate.getTime()) || checkDate.toISOString().slice(0, 10) !== dateStr) {
			return uni.showToast({ title: '填写的日期不存在', icon: 'none' });
		}

		executeAdd(dateStr);
	};

	// 方式二：日历选择
	const handleAddHoliday = (e) => {
		executeAdd(e.detail.value);
	};

	const handleRemove = (dateStr) => {
		uni.showModal({
			title: '确认移除',
			content: `移除后访客可预约 ${dateStr}，确定操作？`,
			success: async (res) => {
				if (res.confirm) {
					uni.showLoading({ title: '移除中...' });
					try {
						const delRes = await deleteHoliday(dateStr);
						if (delRes.code === '00000') {
							holidays.value = holidays.value.filter(d => d !== dateStr);
							uni.showToast({ title: '已移除', icon: 'success' });
						} else {
							uni.showToast({ title: delRes.msg || '移除失败', icon: 'none' });
						}
					} catch (error) {
						uni.showToast({ title: '网络异常', icon: 'none' });
					} finally {
						uni.hideLoading();
					}
				}
			}
		});
	};
</script>

<style scoped>
	.container {
		height: 100vh;
		background-color: #f5f7fa;
		display: flex;
		flex-direction: column;
	}

	.header-card {
		background-color: #fff;
		margin: 20rpx;
		border-radius: 12rpx;
		padding: 30rpx;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.02);
		display: flex;
		flex-direction: column;
		gap: 24rpx;
	}

	.text-wrap {
		display: flex;
		flex-direction: column;
	}

	.main-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 8rpx;
	}

	.desc {
		font-size: 24rpx;
		color: #999;
		line-height: 1.4;
	}

	.action-panel {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
		width: 100%;
	}

	/* 优化后的三格排版 */
	.date-input-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		width: 100%;
	}

	.date-boxes {
		display: flex;
		align-items: center;
		background: #f8f9fa;
		border: 1px solid #e5e5e5;
		border-radius: 8rpx;
		padding: 0 16rpx;
		height: 76rpx;
		box-sizing: border-box;
	}

	.date-box {
		text-align: center;
		font-size: 28rpx;
		color: #333;
		font-family: 'Courier New', Courier, monospace;
		height: 100%;
	}
	
	.year { width: 80rpx; }
	.month { width: 50rpx; }
	.day { width: 50rpx; }

	.separator {
		color: #999;
		margin: 0 8rpx;
		font-weight: bold;
	}

	.action-btn-inline {
		background-color: #245381;
		color: #fff;
		font-size: 26rpx;
		font-weight: bold;
		height: 76rpx;
		line-height: 76rpx;
		padding: 0 36rpx;
		border-radius: 8rpx;
		white-space: nowrap;
	}
	.action-btn-inline:active {
		opacity: 0.9;
	}

	.add-btn-full {
		width: 100%;
		height: 76rpx;
		line-height: 76rpx;
		background-color: #f5f5f5;
		color: #555;
		font-size: 26rpx;
		text-align: center;
		border-radius: 8rpx;
		border: 1rpx dashed #d9d9d9;
		box-sizing: border-box;
	}
	.add-btn-full:active {
		background-color: #e8e8e8;
	}

	/* 列表样式 */
	.list-area {
		flex: 1;
		padding: 0 20rpx;
	}

	.empty {
		text-align: center;
		margin-top: 150rpx;
		color: #999;
		font-size: 28rpx;
	}

	.card {
		background-color: #fff;
		padding: 30rpx;
		border-radius: 12rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.02);
	}

	.card-body {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.left-info {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}

	.calendar-icon {
		font-size: 32rpx;
	}

	.date-val {
		font-size: 30rpx;
		font-weight: bold;
		color: #333;
		font-family: 'Courier New', Courier, monospace;
	}

	.status-tag {
		font-size: 20rpx;
		padding: 4rpx 12rpx;
		border-radius: 6rpx;
	}

	.status-off {
		background-color: #fff1f0;
		color: #f5222d;
	}

	.action-btn {
		font-size: 26rpx;
		font-weight: 500;
	}

	.delete {
		color: #f5222d;
	}
</style>