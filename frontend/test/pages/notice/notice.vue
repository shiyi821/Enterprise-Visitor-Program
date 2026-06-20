<template>
	<view class="main">
		<view class="top-bg">
			<view class="page-title">消息中心</view>
			<view class="page-desc">您的预约审批进度与系统通知</view>
		</view>

		<view class="content">
			<view class="empty-box" v-if="messageList.length === 0">
				<image class="empty-img" src="https://img.icons8.com/ios/100/A6B1BB/box-important--v1.png"></image>				<view class="empty-text">暂无最新消息</view>
			</view>

			<view class="message-list" v-else>
				<view 
					class="msg-card" 
					v-for="item in messageList" 
					:key="item.id" 
					@click="handleMessageClick(item)"
				>
					<view class="unread-dot" v-if="item.isRead === 0"></view>

					<view class="card-header">
						<view class="title-wrap">
							<image v-if="item.type === 1" class="icon-type" src="https://img.icons8.com/ios-filled/50/2ECC71/ok--v1.png" />
							<image v-else-if="item.type === 2" class="icon-type" src="https://img.icons8.com/ios-filled/50/E74C3C/cancel.png" />
							<image v-else class="icon-type" src="https://img.icons8.com/ios-filled/50/3B5BDB/bell.png" />
							
							<text class="title">{{ item.title || '系统通知' }}</text>
						</view>
					</view>
					
					<view class="card-body">
						<text class="desc">来源：{{ item.publisherName || '系统中心' }}</text>
						<text class="time">{{ item.publishTime || '刚刚' }}</text>
					</view>
				</view>

				<view class="load-more-text" v-if="messageList.length > 0">
					<text>{{ hasMore ? '正在加载更多...' : '已经到底啦~' }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { request } from '@/utils/request.js'; // 👈 确保引入了你封装的请求工具

export default {
	data() {
		return {
			messageList: [], // 消息数据存储数组
			queryParams: {
				pageNum: 1,
				pageSize: 10
			},
			hasMore: true, // 是否还有更多数据
			loading: false // 防止并发请求锁
		};
	},

	// 🛠️ 关键生命周期：每次切回该页面（比如查看详情返回后），都自动重新加载列表刷新未读状态
	onShow() {
		this.initList();
	},

	// 🌧️ 下拉刷新
	onPullDownRefresh() {
		this.initList(() => {
			uni.stopPullDownRefresh();
			uni.showToast({ title: '刷新成功', icon: 'none' });
		});
	},

	// 🚀 上拉加载下一页
	onReachBottom() {
		if (!this.hasMore || this.loading) return;
		this.queryParams.pageNum++;
		this.fetchNoticeList();
	},

	methods: {
		/**
		 * 初始化/重置列表
		 */
		initList(callback = null) {
			this.queryParams.pageNum = 1;
			this.messageList = [];
			this.hasMore = true;
			this.fetchNoticeList(callback);
		},

		/**
		 * 核心方法：拉取后端消息
		 */
		fetchNoticeList(callback = null) {
			if (this.loading) return;
			this.loading = true;

			request({
				url: '/api/v1/notices/my',
				method: 'GET',
				data: this.queryParams
			})
			.then(res => {
				// 💡 修复 Bug 点：后端返回的是 PageResult 结构，需要通过 res.data.list 拿取数组
				const list = res.data?.list || [];
				const total = res.data?.total || 0;

				if (this.queryParams.pageNum === 1) {
					this.messageList = list;
				} else {
					this.messageList = this.messageList.concat(list);
				}

				// 判断是否加载完了所有数据
				if (this.messageList.length >= total) {
					this.hasMore = false;
				}
			})
			.catch(err => {
				console.error('获取通知列表失败：', err);
				uni.showToast({ title: '获取列表失败', icon: 'none' });
			})
			.finally(() => {
				this.loading = false;
				if (callback) callback();
			});
		},

		/**
		 * 点击消息卡片事件
		 */
		handleMessageClick(item) {
			// 1. 如果消息未读(0)，先调后端接口将其变更为已读状态
			if (item.isRead === 0) {
				request({
					url: `/api/v1/notices/${item.id}/read`,
					method: 'PUT'
				}).then(() => {
					item.isRead = 1; // 局部改变状态，红点瞬间消失
				}).catch(e => console.error('标记已读失败', e));
			}

			// 2. 携带通知 ID 跳转到消息详情页 detail.vue
			uni.navigateTo({
				url: `/pages/notice/detail?id=${item.id}`
			});
		}
	}
};
</script>

<style scoped>
.main {
	min-height: 100vh;
	background-color: #F8F9FA;
}

/* 顶部渐变背景 */
.top-bg {
	background: linear-gradient(135deg, #3B5BDB 0%, #228BE6 100%);
	padding: 60rpx 40rpx 80rpx;
	color: #FFFFFF;
}

.page-title {
	font-size: 44rpx;
	font-weight: bold;
	margin-bottom: 12rpx;
	letter-spacing: 2rpx;
}

.page-desc {
	font-size: 26rpx;
	opacity: 0.85;
}

/* 内容外层 */
.content {
	padding: 30rpx;
	margin-top: -40rpx;
}

/* 消息卡片 */
.msg-card {
	background-color: #FFFFFF;
	border-radius: 20rpx;
	padding: 35rpx 30rpx 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 8rpx 20rpx rgba(149, 157, 165, 0.06);
	position: relative;
	transition: all 0.2s ease;
}

.msg-card:active {
	transform: scale(0.99);
	background-color: #FAFAFA;
}

/* 未读红点 */
.unread-dot {
	position: absolute;
	top: 35rpx;
	right: 30rpx;
	width: 14rpx;
	height: 14rpx;
	background-color: #E74C3C;
	border-radius: 50%;
	box-shadow: 0 0 10rpx rgba(231, 76, 60, 0.5);
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 20rpx;
}

.title-wrap {
	display: flex;
	align-items: center;
	flex: 1;
	padding-right: 40rpx; 
}

.icon-type {
	width: 38rpx;
	height: 38rpx;
	margin-right: 18rpx;
	flex-shrink: 0;
}

.title {
	font-size: 30rpx;
	font-weight: bold;
	color: #2C3E50;
	line-height: 1.4;
}

.card-body {
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 24rpx;
	color: #95A5A6;
	border-top: 1rpx solid #F2F4F5;
	padding-top: 15rpx;
	margin-top: 15rpx;
}

.time {
	font-size: 22rpx;
}

/* 空状态样式 */
.empty-box {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding-top: 150rpx;
}

.empty-img {
	width: 140rpx;
	height: 140rpx;
	margin-bottom: 24rpx;
	opacity: 0.7;
}

.empty-text {
	font-size: 28rpx;
	color: #95A5A6;
}

/* 底线提示文本 */
.load-more-text {
	text-align: center;
	font-size: 24rpx;
	color: #BDC3C7;
	padding: 20rpx 0 40rpx;
}
</style>