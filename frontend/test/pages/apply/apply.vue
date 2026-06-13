<template>
	<view class="container">
		<view class="form-group">
			<text class="label"><text class="required">*</text>申请来访日期</text>
			<picker mode="date" @change="onDateChange">
				<view class="input picker-text">
					{{ formData.visitDate || '请选择申请来访日期' }}
				</view>
			</picker>
		</view>

		<view class="form-group">
			<text class="label"><text class="required">*</text>申请来访时间点</text>
			<picker mode="time" @change="(e) => formData.visitTime = e.detail.value">
				<view class="input picker-text">
					{{ formData.visitTime || '请选择时间点 (24小时制)' }}
				</view>
			</picker>
		</view>

		<view class="form-group">
			<text class="label"><text class="required">*</text>申请人姓名</text>
			<input class="input" placeholder="请输入申请人姓名" v-model="formData.applicantName" />
		</view>

		<view class="form-group">
			<text class="label"><text class="required">*</text>申请人电话</text>
			<input class="input" type="number" maxlength="11" placeholder="请输入11位手机号"
				v-model="formData.applicantPhone" />
		</view>

		<view class="form-group">
			<text class="label"><text class="required">*</text>申请人身份证号</text>
			<input class="input" type="text" maxlength="18" placeholder="请输入18位身份证号码"
				v-model="formData.applicantIdCard" />
		</view>

		<view class="form-group">
			<text class="label"><text class="required">*</text>申请人性别</text>
			<radio-group class="radio-group" @change="(e) => formData.applicantGender = e.detail.value">
				<label class="radio-label">
					<radio value="男" :checked="formData.applicantGender === '男'" color="#245381" /> 男
				</label>
				<label class="radio-label">
					<radio value="女" :checked="formData.applicantGender === '女'" color="#245381" /> 女
				</label>
			</radio-group>
		</view>

		<view class="form-group">
			<text class="label"><text class="required">*</text>申请人单位</text>
			<input class="input" placeholder="请输入申请人所在单位" v-model="formData.applicantCompany" />
		</view>

		<view class="form-group">
			<text class="label"><text class="required">*</text>来访人数 (包含申请人)</text>
			<view class="stepper">
				<view class="step-btn" @click="changeVisitorCount(-1)">-</view>
				<input class="step-input" type="number" v-model.number="formData.visitorCount"
					@blur="validateVisitorCount" />
				<view class="step-btn" @click="changeVisitorCount(1)">+</view>
			</view>
		</view>

		<view class="form-group" v-if="formData.visitorCount > 1">
			<text class="label">同行来访人姓名 (选填)</text>
			<view class="companion-grid">
				<input class="input companion-input" v-for="(item, index) in companionNames" :key="index"
					v-model="companionNames[index]" :placeholder="'同行人 ' + (index + 1)" />
			</view>
		</view>

		<view class="form-group search-group">
			<text class="label"><text class="required">*</text>到访单位</text>
			<input class="input" :class="{ 'disabled-input': isAssistMode }" :disabled="isAssistMode"
				:placeholder="isAssistMode ? '' : '点击查看全部或输入搜索'" v-model="formData.visitDepartment"
				@input="onSearchDepartment" @focus="onDeptFocus" @blur="hideDropdown('dept')" />
			<scroll-view scroll-y class="dropdown-list"
				v-if="!isAssistMode && showDeptDropdown && deptOptions.length > 0">
				<view class="dropdown-item" v-for="item in deptOptions" :key="item.id" @click="selectDepartment(item)">
					{{ item.name }}
				</view>
			</scroll-view>
		</view>

		<view class="form-group search-group">
			<text class="label"><text class="required">*</text>被访人</text>
			<input class="input" :class="{ 'disabled-input': isAssistMode }" :disabled="isAssistMode"
				:placeholder="isAssistMode ? '' : '点击查看全部或输入搜索'" v-model="formData.hostName" @input="onSearchHost"
				@focus="onHostFocus" @blur="hideDropdown('host')" />
			<scroll-view scroll-y class="dropdown-list"
				v-if="!isAssistMode && showHostDropdown && hostOptions.length > 0">
				<view class="dropdown-item" v-for="item in hostOptions" :key="item.id" @click="selectHost(item)">
					{{ item.name }}
				</view>
			</scroll-view>
		</view>

		<view class="form-group">
			<text class="label"><text class="required">*</text>来访事由</text>
			<textarea class="input textarea" placeholder="请输入来访事由" v-model="formData.reason"></textarea>
		</view>

		<button class="submit-btn" @click="submitApply">提交预约审批</button>
	</view>
</template>

<script setup>
	import {
		ref,
		watch
	} from 'vue';
	import {
		onLoad,
		onShow,
		onHide
	} from '@dcloudio/uni-app';

	const isAssistMode = ref(false);
	const systemHolidays = ref([]); // 存放系统设置的节假日

	onShow(() => {
		// 1. 处理辅助预约状态
		const isAssist = uni.getStorageSync('isAssistMode');
		if (isAssist === true) {
			isAssistMode.value = true;
			formData.value.visitDepartment = '技术部';
			formData.value.hostName = '李总';
		}

		// 2. 加载节假日数据
		const savedHolidays = uni.getStorageSync('system_holidays');
		if (savedHolidays && Array.isArray(savedHolidays)) {
			systemHolidays.value = savedHolidays;
		}
	});

	onHide(() => {
		if (isAssistMode.value) {
			uni.removeStorageSync('isAssistMode');
			isAssistMode.value = false;
			formData.value.visitDepartment = '';
			formData.value.hostName = '';
		}
	});

	onLoad((options) => {
		if (options && options.mode === 'assist') {
			uni.setStorageSync('isAssistMode', true);
		}
	});

	const getInitialData = () => ({
		visitDate: '',
		visitTime: '',
		applicantName: '',
		applicantPhone: '',
		applicantIdCard: '',
		applicantGender: '',
		applicantCompany: '',
		visitorCount: 1,
		visitDepartment: '',
		hostName: '',
		reason: ''
	});

	const formData = ref(getInitialData());
	const companionNames = ref([]);

	// ======== 核心拦截逻辑：节假日判断 ========
	const onDateChange = (e) => {
		const selectedDate = e.detail.value;

		// 如果选中的日期在节假日数组中，立刻拦截
		if (systemHolidays.value.includes(selectedDate)) {
			uni.showModal({
				title: '日期不可选',
				content: `管理员已将 ${selectedDate} 设为休息日，暂不开放来访预约，请重新选择日期。`,
				showCancel: false, // 只有确定按钮
				confirmText: '我知道了'
			});
			// 清空选择
			formData.value.visitDate = '';
		} else {
			// 正常日期，允许赋值
			formData.value.visitDate = selectedDate;
		}
	};
	// ===========================================

	const changeVisitorCount = (num) => {
		let current = parseInt(formData.value.visitorCount) || 1;
		current += num;
		if (current < 1) current = 1;
		formData.value.visitorCount = current;
	};

	const validateVisitorCount = () => {
		let current = parseInt(formData.value.visitorCount);
		if (isNaN(current) || current < 1) {
			formData.value.visitorCount = 1;
		}
	};

	watch(() => formData.value.visitorCount, (newVal) => {
		const companionCount = Math.max(0, newVal - 1);
		if (companionNames.value.length < companionCount) {
			while (companionNames.value.length < companionCount) {
				companionNames.value.push('');
			}
		} else if (companionNames.value.length > companionCount) {
			companionNames.value.splice(companionCount);
		}
	});

	const showDeptDropdown = ref(false);
	const deptOptions = ref([]);
	let deptTimer = null;

	const fetchDepartments = (keyword = '') => {
		if (keyword) {
			deptOptions.value = [{
				id: 11,
				name: `${keyword}技术部`
			}, {
				id: 12,
				name: `${keyword}业务部`
			}];
		} else {
			deptOptions.value = [{
				id: 1,
				name: '集团总部'
			}, {
				id: 2,
				name: '技术研发中心'
			}, {
				id: 3,
				name: '市场运营部'
			}];
		}
	};

	const onSearchDepartment = (e) => {
		if (isAssistMode.value) return;
		const keyword = e.detail.value;
		clearTimeout(deptTimer);
		deptTimer = setTimeout(() => {
			fetchDepartments(keyword);
		}, 300);
	};

	const onDeptFocus = () => {
		if (isAssistMode.value) return;
		showDeptDropdown.value = true;
		fetchDepartments(formData.value.visitDepartment);
	};

	const selectDepartment = (item) => {
		formData.value.visitDepartment = item.name;
		showDeptDropdown.value = false;
	};

	const showHostDropdown = ref(false);
	const hostOptions = ref([]);
	let hostTimer = null;

	const fetchHosts = (keyword = '') => {
		if (keyword) {
			hostOptions.value = [{
				id: 11,
				name: `${keyword}总监`
			}, {
				id: 12,
				name: `${keyword}经理`
			}];
		} else {
			hostOptions.value = [{
				id: 1,
				name: '张三 (前台)'
			}, {
				id: 2,
				name: '李四 (行政)'
			}, {
				id: 3,
				name: '李总 (技术)'
			}];
		}
	};

	const onSearchHost = (e) => {
		if (isAssistMode.value) return;
		const keyword = e.detail.value;
		clearTimeout(hostTimer);
		hostTimer = setTimeout(() => {
			fetchHosts(keyword);
		}, 300);
	};

	const onHostFocus = () => {
		if (isAssistMode.value) return;
		showHostDropdown.value = true;
		fetchHosts(formData.value.hostName);
	};

	const selectHost = (item) => {
		formData.value.hostName = item.name;
		showHostDropdown.value = false;
	};

	const hideDropdown = (type) => {
		setTimeout(() => {
			if (type === 'dept') showDeptDropdown.value = false;
			if (type === 'host') showHostDropdown.value = false;
		}, 200);
	};

	const submitApply = () => {
		const requiredFields = [{
				key: 'visitDate',
				msg: '请选择来访日期'
			}, {
				key: 'visitTime',
				msg: '请选择来访时间'
			},
			{
				key: 'applicantName',
				msg: '请输入申请人姓名'
			}, {
				key: 'applicantPhone',
				msg: '请输入申请人电话'
			},
			{
				key: 'applicantIdCard',
				msg: '请输入申请人身份证号'
			}, {
				key: 'applicantGender',
				msg: '请选择性别'
			},
			{
				key: 'applicantCompany',
				msg: '请输入申请人单位'
			}, {
				key: 'visitDepartment',
				msg: '请选择到访单位'
			},
			{
				key: 'hostName',
				msg: '请选择被访人'
			}, {
				key: 'reason',
				msg: '请输入来访事由'
			}
		];

		for (let field of requiredFields) {
			if (!formData.value[field.key]) return uni.showToast({
				title: field.msg,
				icon: 'none'
			});
		}

		const submitData = {
			...formData.value,
			companions: companionNames.value.filter(name => name.trim() !== '').join(','),
			isAssist: isAssistMode.value
		};

		uni.showToast({
			title: '申请已提交',
			icon: 'success',
			duration: 2000
		});

		setTimeout(() => {
			if (isAssistMode.value) {
				const currentHost = formData.value.hostName;
				const currentDept = formData.value.visitDepartment;
				formData.value = getInitialData();
				formData.value.hostName = currentHost;
				formData.value.visitDepartment = currentDept;
			} else {
				formData.value = getInitialData();
			}
			companionNames.value = [];
		}, 1000);
	};
</script>

<style lang="scss" scoped>
	:global(page::-webkit-scrollbar),
	::-webkit-scrollbar {
		display: none !important;
		width: 0 !important;
		height: 0 !important;
		color: transparent !important;
		background: transparent !important;
	}

	.container {
		padding: 20px;
		background-color: #f8f9fa;
		min-height: 100vh;
	}

	.form-group {
		margin-bottom: 20px;
	}

	.form-group .label {
		display: block;
		margin-bottom: 8px;
		font-size: 14px;
		color: #333;
		font-weight: 500;
	}

	.form-group .required {
		color: #ff4d4f;
		margin-right: 4px;
	}

	.form-group .input {
		height: 44px;
		border: 1px solid #e5e5e5;
		border-radius: 6px;
		padding: 0 12px;
		background-color: #fff;
		font-size: 14px;
		box-sizing: border-box;
		width: 100%;
	}

	.disabled-input {
		background-color: #f5f5f5 !important;
		color: #888 !important;
		border-color: #e5e5e5 !important;
	}

	.form-group .textarea {
		height: 80px;
		padding: 12px;
	}

	.form-group .picker-text {
		line-height: 44px;
		color: #333;
	}

	.radio-group {
		display: flex;
		align-items: center;
		height: 44px;
	}

	.radio-group .radio-label {
		margin-right: 30px;
		display: flex;
		align-items: center;
		font-size: 14px;
	}

	.radio-group .radio-label radio {
		transform: scale(0.8);
	}

	.stepper {
		display: flex;
		align-items: center;
		width: 120px;
		height: 36px;
		border: 1px solid #e5e5e5;
		border-radius: 6px;
		background-color: #fff;
		overflow: hidden;
	}

	.stepper .step-btn {
		width: 36px;
		height: 100%;
		display: flex;
		justify-items: center;
		align-items: center;
		justify-content: center;
		background-color: #f5f5f5;
		font-size: 18px;
		color: #666;
	}

	.stepper .step-btn:active {
		background-color: #e0e0e0;
	}

	.stepper .step-input {
		flex: 1;
		height: 100%;
		text-align: center;
		font-size: 14px;
		border-left: 1px solid #e5e5e5;
		border-right: 1px solid #e5e5e5;
	}

	.companion-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 10px;
	}

	.companion-grid .companion-input {
		width: calc(33.33% - 6.66px);
		text-align: center;
		padding: 0 4px;
	}

	.search-group {
		position: relative;
	}

	.search-group .dropdown-list {
		position: absolute;
		top: 75px;
		left: 0;
		width: 100%;
		max-height: 200px;
		background-color: #fff;
		border: 1px solid #e5e5e5;
		border-radius: 6px;
		box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
		z-index: 10;
	}

	.search-group .dropdown-list .dropdown-item {
		padding: 12px;
		font-size: 14px;
		color: #333;
		border-bottom: 1px solid #f0f0f0;
	}

	.search-group .dropdown-list .dropdown-item:last-child {
		border-bottom: none;
	}

	.search-group .dropdown-list .dropdown-item:active {
		background-color: #f8f9fa;
	}

	.submit-btn {
		margin-top: 40px;
		margin-bottom: 40px;
		background-color: #245381;
		color: #fff;
		border-radius: 6px;
		font-size: 16px;
	}

	.submit-btn::after {
		border: none;
	}

	.submit-btn:active {
		opacity: 0.9;
	}
</style>