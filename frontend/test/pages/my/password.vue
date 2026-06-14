<template>
	<view class="container">
		<view class="form-card">
			<view class="form-group">
				<text class="label">旧密码</text>
				<input 
					class="input" 
					password
					autocomplete="new-password"
					:value="oldPassword"
					@input="e => oldPassword = e.detail.value"
					placeholder="请输入当前密码"
				/>
			</view>
			
			<view class="form-group">
				<text class="label">新密码</text>
				<input 
					class="input" 
					password
					autocomplete="new-password"
					:value="newPassword"
					@input="e => newPassword = e.detail.value"
					placeholder="请输入新密码"
				/>
			</view>
			
			<view class="form-group">
				<text class="label">确认新密码</text>
				<input 
					class="input" 
					password
					autocomplete="new-password"
					:value="confirmPassword"
					@input="e => confirmPassword = e.detail.value"
					placeholder="请再次输入新密码"
				/>
			</view>
		</view>
		
		<button class="submit-btn" :disabled="isSubmitting" @click="handleSubmit">确认修改</button>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { request } from '@/utils/request.js';

const oldPassword = ref('');
const newPassword = ref('');
const confirmPassword = ref('');
const isSubmitting = ref(false);

// 清洗密码：去除不可见字符和首尾空格
const cleanPassword = (str) => {
	if (!str) return '';
	let result = '';
	for (let i = 0; i < str.length; i++) {
		const code = str.charCodeAt(i);
		if (code >= 32 && code <= 126) {
			result += str[i];
		}
	}
	return result.trim();
};

const handleSubmit = () => {
	if (isSubmitting.value) return;

	const oldPwd = cleanPassword(oldPassword.value);
	const newPwd = cleanPassword(newPassword.value);
	const confirmPwd = cleanPassword(confirmPassword.value);

	if (!oldPwd) {
		return uni.showToast({ title: '请输入旧密码', icon: 'none' });
	}
	if (!newPwd) {
		return uni.showToast({ title: '请输入新密码', icon: 'none' });
	}
	if (newPwd.length < 6) {
		return uni.showToast({ title: '新密码长度不能少于6位', icon: 'none' });
	}
	if (newPwd !== confirmPwd) {
		return uni.showToast({ title: '两次输入的新密码不一致', icon: 'none' });
	}

	uni.showModal({
		title: '确认修改',
		content: '确定要修改登录密码吗？修改后需要使用新密码重新登录。',
		confirmColor: '#245381',
		success: (res) => {
			if (res.confirm) {
				submitPassword(oldPwd, newPwd);
			}
		}
	});
};

const submitPassword = async (oldPwd, newPwd) => {
	isSubmitting.value = true;
	uni.showLoading({ title: '修改中...', mask: true });
	
	try {
		const res = await request({
			url: '/api/v1/users/password',
			method: 'PUT',
			data: {
				oldPassword: oldPwd,
				newPassword: newPwd,
				confirmPassword: newPwd
			}
		});

		if (res.code === '00000') {
			uni.showToast({
				title: '密码修改成功',
				icon: 'success',
				duration: 1500
			});

			// 清除所有本地登录缓存，强制重新登录
			uni.removeStorageSync('token');
			uni.removeStorageSync('userInfo');
			uni.removeStorageSync('userRole');
			uni.removeStorageSync('is_new_user_flag');

			setTimeout(() => {
				// 清空页面栈，直接跳登录页，无法返回
				uni.reLaunch({
					url: '/pages/login/login'
				});
			}, 1500);
		} else {
			uni.showToast({
				title: res.msg || '修改失败，请检查旧密码是否正确',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('修改密码失败:', error);
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		uni.hideLoading();
		isSubmitting.value = false;
	}
};
</script>

<style lang="scss" scoped>
.container {
	min-height: 100vh;
	background-color: #f5f6f8;
	padding: 20px;
	box-sizing: border-box;
}

.form-card {
	background-color: #fff;
	border-radius: 12px;
	padding: 10px 20px;
	margin-bottom: 40px;
}

.form-group {
	display: flex;
	align-items: center;
	padding: 18px 0;
	border-bottom: 1px solid #f0f0f0;
	
	&:last-child {
		border-bottom: none;
	}
}

.label {
	width: 100px;
	font-size: 15px;
	color: #333;
	flex-shrink: 0;
}

.input {
	flex: 1;
	font-size: 15px;
	color: #333;
}

.submit-btn {
	width: 100%;
	height: 48px;
	line-height: 48px;
	background-color: #245381;
	color: #fff;
	border-radius: 24px;
	font-size: 16px;
	
	&::after {
		border: none;
	}
	
	&:active {
		opacity: 0.85;
	}
	
	&[disabled] {
		opacity: 0.6;
	}
}
</style>