<template>
	<view class="main">
		<view class="top-bg">
			<view class="page-title">{{ isLogin ? '欢迎回来' : '注册新账号' }}</view>
			<view class="page-desc">智能访客预约管理系统</view>
		</view>

		<view class="content">
			<view class="form-card">
				
				<view class="tabs">
					<view class="tab-item" :class="{ active: isLogin }" @click="switchTab(true)">登录</view>
					<view class="tab-item" :class="{ active: !isLogin }" @click="switchTab(false)">注册</view>
				</view>

				<view class="form-item">
					<view class="icon-wrap">
						<image class="icon" src="https://img.icons8.com/ios-filled/50/95A5A6/phone.png" />
					</view>
					<input class="input-box" type="number" maxlength="11" v-model="formData.phone" placeholder="请输入11位手机号码" placeholder-class="ph-color" />
				</view>

				<view class="form-item">
					<view class="icon-wrap">
						<image class="icon" src="https://img.icons8.com/ios-filled/50/95A5A6/lock.png" />
					</view>
					<input class="input-box" type="password" v-model="formData.password" placeholder="请输入密码" placeholder-class="ph-color" />
				</view>

				<view class="form-item" v-if="!isLogin">
					<view class="icon-wrap">
						<image class="icon" src="https://img.icons8.com/ios-filled/50/95A5A6/check-all.png" />
					</view>
					<input class="input-box" type="password" v-model="formData.confirmPassword" placeholder="请再次输入密码确认" placeholder-class="ph-color" />
				</view>

				<button class="submit-btn" @click="handleSubmit">
					{{ isLogin ? '立即登录' : '同意协议并注册' }}
				</button>
				
				<view class="footer-links" v-if="isLogin">
					<text class="link-text">忘记密码？</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, reactive } from 'vue';
// 引入刚刚封装好的后端接口调用函数
import { loginApi, registerApi } from '../../api/auth.js';

// 控制当前是登录还是注册状态 (true=登录, false=注册)
const isLogin = ref(true);

const formData = reactive({
	phone: '',
	password: '',
	confirmPassword: ''
});

// 切换登录/注册模式
const switchTab = (status) => {
	isLogin.value = status;
	// 切换时清空密码框
	formData.password = '';
	formData.confirmPassword = '';
};

// 提交表单前的基础验证
const handleSubmit = () => {
	if (!formData.phone) {
		return uni.showToast({ title: '请输入手机号', icon: 'none' });
	}
	const phoneReg = /^1[3-9]\d{9}$/;
	if (!phoneReg.test(formData.phone)) {
		return uni.showToast({ title: '手机号格式不正确', icon: 'none' });
	}
	if (!formData.password) {
		return uni.showToast({ title: '请输入密码', icon: 'none' });
	}

	if (!isLogin.value) {
		// 注册模式下的额外校验
		if (formData.password !== formData.confirmPassword) {
			return uni.showToast({ title: '两次输入的密码不一致', icon: 'none' });
		}
		doRegister();
	} else {
		doLogin();
	}
};

// ========================
// 联调后端的注册逻辑
// ========================
const doRegister = async () => {
	uni.showLoading({ title: '注册中...' });
	try {
		const res = await registerApi({
			username: formData.phone, // 把手机号当做账号传给后端
			password: formData.password,
			mobile: formData.phone
		});
		uni.hideLoading();
		
		if (res.code === '00000') {
			uni.showToast({ title: '注册成功，请登录', icon: 'success' });
			// 注册成功后自动切回登录 Tab
			setTimeout(() => { switchTab(true); }, 1500);
		} else {
			uni.showToast({ title: res.msg || '注册失败', icon: 'none' });
		}
	} catch (err) {
		uni.hideLoading();
	}
};

// ========================
// 联调后端的登录逻辑
// ========================
const doLogin = async () => {
	uni.showLoading({ title: '登录中...' });
	try {
		const res = await loginApi({
			username: formData.phone,
			password: formData.password
		});
		uni.hideLoading();
		
		if (res.code === '00000') {
			// 登录成功，把 Token 存到前端本地缓存中
			uni.setStorageSync('token', res.data.accessToken);
			uni.showToast({ title: '登录成功', icon: 'success' });
			
			// 登录成功后跳转到“我的”个人中心页面
			setTimeout(() => {
				uni.switchTab({ url: '/pages/my/my' });
			}, 1000);
		} else {
			uni.showToast({ title: res.msg || '账号或密码错误', icon: 'none' });
		}
	} catch (err) {
		uni.hideLoading();
	}
};
</script>

<style scoped>
:global(page::-webkit-scrollbar), ::-webkit-scrollbar {
	display: none !important;
	width: 0 !important;
	height: 0 !important;
	color: transparent !important;
	background: transparent !important;
}
	
:global(page) {
	background-color: #F5F7FA;
}

.main {
	min-height: 100vh;
	background-color: #F5F7FA;
}

.top-bg {
	width: 100%;
	height: 380rpx;
	background-color: #245381;
	padding: 100rpx 50rpx 0;
	box-sizing: border-box;
}

.page-title {
	font-size: 56rpx;
	font-weight: bold;
	color: #FFFFFF;
	margin-bottom: 20rpx;
	letter-spacing: 2rpx;
}

.page-desc {
	font-size: 28rpx;
	color: rgba(255, 255, 255, 0.8);
}

.content {
	padding: 0 40rpx;
	margin-top: -120rpx;
	position: relative;
	z-index: 2;
}

.form-card {
	background-color: #FFFFFF;
	border-radius: 24rpx;
	padding: 50rpx 40rpx;
	box-shadow: 0 15rpx 40rpx rgba(36, 83, 129, 0.08);
}

.tabs {
	display: flex;
	justify-content: space-around;
	margin-bottom: 60rpx;
	border-bottom: 2rpx solid #F1F2F6;
}

.tab-item {
	font-size: 32rpx;
	color: #95A5A6;
	padding-bottom: 20rpx;
	position: relative;
	transition: all 0.3s;
}

.tab-item.active {
	color: #245381;
	font-weight: bold;
}

.tab-item.active::after {
	content: '';
	position: absolute;
	bottom: -2rpx;
	left: 50%;
	transform: translateX(-50%);
	width: 40rpx;
	height: 6rpx;
	background-color: #245381;
	border-radius: 6rpx;
}

.form-item {
	display: flex;
	align-items: center;
	height: 100rpx;
	background-color: #F8F9FA;
	border-radius: 16rpx;
	margin-bottom: 30rpx;
	padding: 0 30rpx;
}

.icon-wrap {
	width: 50rpx;
	display: flex;
	justify-content: flex-start;
}

.icon {
	width: 36rpx;
	height: 36rpx;
	opacity: 0.7;
}

.input-box {
	flex: 1;
	font-size: 30rpx;
	color: #2C3E50;
	height: 100%;
}

.ph-color {
	color: #A6B1BB;
}

.submit-btn {
	width: 100%;
	height: 96rpx;
	line-height: 96rpx;
	background-color: #245381;
	color: #FFFFFF;
	font-size: 34rpx;
	font-weight: bold;
	border-radius: 48rpx;
	margin-top: 60rpx;
	box-shadow: 0 10rpx 20rpx rgba(36, 83, 129, 0.2);
}

.submit-btn:active {
	transform: scale(0.98);
}

.submit-btn::after {
	border: none;
}

.footer-links {
	display: flex;
	justify-content: center;
	margin-top: 40rpx;
}

.link-text {
	font-size: 26rpx;
	color: #95A5A6;
}
</style>