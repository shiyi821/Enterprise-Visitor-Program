<template>
	<view class="main">
		<view class="detail-card" v-if="msgData.id">
			
			<view class="type-tag-wrap">
				<view class="tag" :class="tagClass">
					{{ typeText }}
				</view>
			</view>

			<view class="title">{{ msgData.title }}</view>

			<view class="meta-info">
				<text class="time">发布时间：{{ msgData.publishTime }}</text>
				<text class="source">来源：系统中心</text>
			</view>

			<view class="divider"></view>

			<view class="content-body">
							<text class="text-p">{{ msgData.content }}</text>
			
							<view class="tip-box success-box" v-if="msgData.type === 1 && (msgData.title.includes('通过') || msgData.title.includes('成功'))">
								<view class="tip-title">💡 快捷通行指引</view>
								<view class="tip-item">1. 到达企业大门时，打开“我的申请”列表。</view>
								<view class="tip-item">2. 点击对应记录展示“通行二维码”。</view>
								<view class="tip-item">3. 将二维码出示给门岗进行核验，核验无误后即可进入。</view>
							</view>
			
							<view class="tip-box reject-box" v-if="msgData.type === 2 && (msgData.title.includes('驳回') || msgData.title.includes('拒绝'))">
								<view class="tip-title">🛠️ 后续建议</view>
								<view class="tip-item">如对审批结果有疑问，请直接联系被访人核实情况，或修改预约信息后重新提交。</view>
							</view>
						</view>
		</view>
	</view>
</template>

<script>
import { request } from '@/utils/request.js';

export default {
	data() {
		return {
			msgData: {}
		};
	},
	computed: {
		// 动态映射 CSS 类名
		tagClass() {
			if (this.msgData.type === 1) return 'success';
			if (this.msgData.type === 2) return 'reject';
			return 'info';
		},
		typeText() {
			if (this.msgData.type === 1) return '审批通过';
			if (this.msgData.type === 2) return '审批拒绝';
			return '系统通知';
		}
	},
	onLoad(options) {
		if (options.id) {
			this.fetchDetail(options.id);
		}
	},
	methods: {
		// detail.vue 中的 fetchDetail 方法
		fetchDetail(id) {
		    // 必须与后端的 @GetMapping("/{id}/detail") 完全一致
		    request({
		        url: `/api/v1/notices/${id}/detail`, 
		        method: 'GET'
		    }).then(res => {
		        this.msgData = res.data || {};
		    });
		}
	}
};
</script>

<style scoped>
.main {
	min-height: 100vh;
	background-color: #F5F7FA;
	padding: 30rpx;
}

.detail-card {
	background-color: #FFFFFF;
	border-radius: 24rpx;
	padding: 40rpx;
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04);
}

.tag {
	display: inline-block;
	font-size: 22rpx;
	font-weight: bold;
	padding: 6rpx 20rpx;
	border-radius: 30rpx;
}

/* 状态颜色风格 */
.tag.success { background-color: #E8F8F5; color: #2ECC71; }
.tag.reject { background-color: #FDEDEC; color: #E74C3C; }
.tag.info { background-color: #EBF5FB; color: #3B5BDB; }

.title {
	font-size: 38rpx;
	font-weight: bold;
	color: #2C3E50;
	margin: 24rpx 0;
	line-height: 1.5;
}

.meta-info {
	font-size: 24rpx;
	color: #95A5A6;
	display: flex;
	gap: 30rpx;
}

.divider {
	height: 1rpx;
	background-color: #F1F3F5;
	margin: 30rpx 0;
}

.text-p {
	font-size: 28rpx;
	color: #4A5568;
	line-height: 1.8;
	word-break: break-all;
}

/* 提示盒子美化 */
.tip-box {
	margin-top: 40rpx;
	padding: 30rpx;
	border-radius: 16rpx;
	background-color: #F8F9FA;
}

.tip-title {
	font-weight: bold;
	font-size: 26rpx;
	margin-bottom: 15rpx;
}

.success-box .tip-title { color: #27AE60; }
.reject-box .tip-title { color: #C0392B; }

.tip-item {
	font-size: 24rpx;
	color: #7F8C8D;
	margin-top: 10rpx;
	line-height: 1.6;
}
</style>