<template>
	<view class="main">
		<view class="detail-card">
			<view class="type-tag-wrap">
				<view class="tag" :class="msgData.type">
					{{ typeText }}
				</view>
			</view>

			<view class="title">{{ msgData.title }}</view>
			
			<view class="meta-info">
				<text class="time">发布时间：{{ msgData.time }}</text>
				<text class="source">来源：系统中心</text>
			</view>

			<view class="divider"></view>

			<view class="content-body">
				<text class="text-p">{{ msgData.content }}</text>
				
				<view class="tip-box" v-if="msgData.type === 'success'">
					<view class="tip-title">💡 快捷通行指引：</view>
					<view class="tip-item">1. 到达企业大门门岗时，点击底部“我的申请”。</view>
					<view class="tip-item">2. 找到通过记录，点击展示“通行二维码”。</view>
					<view class="tip-item">3. 将二维码对准核验设备即可快速入园。</view>
				</view>
				
				<view class="tip-box reject-box" v-if="msgData.type === 'reject'">
					<view class="tip-title">🛠️ 后续操作建议：</view>
					<view class="tip-item">1. 请与被访人电话确认可到访时间。</view>
					<view class="tip-item">2. 重新提交一份预约表单即可。</view>
				</view>
			</view>
		</view>

		<view class="btn-group">
			<button class="back-btn" @click="goBack">返回上一页</button>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const mockDatabase = [
	{
		id: 1,
		type: 'success',
		title: '访客预约审批通过',
		time: '2026-06-12 14:15',
		content: '您提交的来访申请（被访人：李经理，技术部）已经过内部安保管理部门和被访人本人的联合审批。您的身份信息已录入今日入园白名单。请在到达大门门岗时，务必出示您的专属通行二维码进行扫码核验。'
	},
	{
		id: 2,
		type: 'reject',
		title: '访客预约被驳回',
		time: '2026-06-11 14:30',
		content: '抱歉，您提交的拜访申请未通过审批。驳回原因：被访员工李经理由于紧急出差，本周五（今日）不在园区内。请您及时与员工取得联系，或重新修改预计到访日期后再次提交申请。'
	},
	{
		id: 3,
		type: 'info',
		title: '系统维护通知',
		time: '2026-06-10 09:00',
		content: '为保障企业数据资产安全，进一步优化系统响应速度，访客预约审批小程序将于本周日凌晨进行底层架构升级，期间提交表单可能会受到影响。'
	}
];

const msgData = ref({});

onLoad((options) => {
	const id = Number(options.id);
	const matched = mockDatabase.find(item => item.id === id);
	if (matched) {
		msgData.value = matched;
	}
});

const typeText = computed(() => {
	if (msgData.value.type === 'success') return '审批通过';
	if (msgData.value.type === 'reject') return '已被驳回';
	return '系统通知';
});

const goBack = () => { uni.navigateBack(); };
</script>

<style scoped>
/* 页面容器 */
.main { min-height: 100vh; background-color: #F5F7FA; padding: 40rpx 30rpx; box-sizing: border-box; }

.detail-card { background-color: #FFFFFF; border-radius: 24rpx; padding: 40rpx; box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.05); }

/* 标签样式 */
.tag { display: inline-block; font-size: 22rpx; font-weight: bold; padding: 6rpx 20rpx; border-radius: 8rpx; }
.tag.success { background-color: #E8F8F5; color: #2ECC71; }
.tag.reject { background-color: #FDEDEC; color: #E74C3C; }
.tag.info { background-color: #EBF5FB; color: #3B5BDB; }

.title { font-size: 40rpx; font-weight: bold; color: #2C3E50; margin: 20rpx 0; }
.meta-info { font-size: 24rpx; color: #95A5A6; display: flex; gap: 30rpx; }
.divider { height: 1px; background-color: #F1F2F6; margin: 30rpx 0; }
.content-body { font-size: 28rpx; color: #34495E; line-height: 1.8; }

/* 提示框 */
.tip-box { background-color: #F8F9FA; border-left: 6rpx solid #3B5BDB; padding: 25rpx; margin-top: 30rpx; border-radius: 8rpx; }
.tip-box.reject-box { border-left-color: #E74C3C; }
.tip-title { font-weight: bold; color: #2C3E50; margin-bottom: 12rpx; font-size: 26rpx; }
.tip-item { font-size: 24rpx; color: #7F8C8D; margin-bottom: 6rpx; }

/* 按钮 */
.btn-group { margin-top: 50rpx; }
.back-btn { width: 100%; height: 90rpx; line-height: 90rpx; background: #FFF; border-radius: 45rpx; border: 1px solid #DCDFE6; font-size: 28rpx; }
</style>