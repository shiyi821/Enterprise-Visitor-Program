<template>
	<view class="container">
		<view class="form-group">
			<text class="label">被访人姓名</text>
			<input class="input" placeholder="请输入被访问员工姓名" v-model="formData.hostName" />
		</view>
		<view class="form-group">
			<text class="label">来访事由</text>
			<input class="input" placeholder="请输入来访事由" v-model="formData.reason" />
		</view>
		<view class="form-group">
			<text class="label">来访时间</text>
			<picker mode="date" @change="onDateChange">
				<view class="input picker-text">
					{{ formData.visitDate || '请选择日期' }}
				</view>
			</picker>
		</view>
		
		<button class="submit-btn" type="primary" @click="submitApply">提交预约审批</button>
	</view>
</template>

<script setup>
import { ref } from 'vue';

const formData = ref({
	hostName: '',
	reason: '',
	visitDate: ''
});

const onDateChange = (e) => {
	formData.value.visitDate = e.detail.value;
};

const submitApply = () => {
	console.log('提交的数据：', formData.value);
	uni.showToast({
		title: '申请已提交',
		icon: 'success'
	});
};
</script>

<style lang="scss">
.container {
	padding: 20px;
}
.form-group {
	margin-bottom: 20px;
	.label {
		display: block;
		margin-bottom: 8px;
		font-size: 16px;
		color: #333;
	}
	.input {
		height: 40px;
		border: 1px solid #e5e5e5;
		border-radius: 6px;
		padding: 0 10px;
		background-color: #fff;
	}
	.picker-text {
		line-height: 40px;
		color: #666;
	}
}
.submit-btn {
	margin-top: 40px;
	background-color: #007AFF;
}
</style>