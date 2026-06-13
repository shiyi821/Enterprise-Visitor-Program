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
				:placeholder="isAssistMode ? '' : '点击选择或输入搜索'" v-model="formData.visitDepartment"
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
			<input class="input" :class="{ 'disabled-input': isAssistMode || !formData.deptId }" :disabled="isAssistMode"
				:placeholder="isAssistMode ? '' : (formData.deptId ? '点击选择或输入搜索' : '请先选择到访单位')" v-model="formData.hostName" @input="onSearchHost"
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
	import { ref, watch } from 'vue';
	import { onLoad, onShow, onHide } from '@dcloudio/uni-app';
	import { request } from '@/utils/request.js';

	const isAssistMode = ref(false);
	const systemHolidays = ref([]);

	// 存放后端拉取的数据字典
	const allDeptOptions = ref([]);
	const allHostOptions = ref([]); // 当前选定部门下的员工列表

	onShow(() => {
		const isAssist = uni.getStorageSync('isAssistMode');
		if (isAssist === true) {
			isAssistMode.value = true;
			formData.value.visitDepartment = '技术部';
			formData.value.hostName = '李总';
		}

		const savedHolidays = uni.getStorageSync('system_holidays');
		if (savedHolidays && Array.isArray(savedHolidays)) {
			systemHolidays.value = savedHolidays;
		}

		// 页面显示时，只拉取部门列表，人员留空
		loadDeptOptions();
	});

	// 1. 加载部门下拉
	const loadDeptOptions = async () => {
		try {
			const deptRes = await request({ url: '/api/v1/depts/options', method: 'GET' });
			if (deptRes && deptRes.data) {
				allDeptOptions.value = deptRes.data.map(item => ({ id: item.value, name: item.label }));
			}
		} catch (err) {
			console.error('加载部门数据失败', err);
		}
	};

	// 💡 2. 联动核心：根据选择的部门ID，去后端拉取该部门下的人员
	const loadHostOptionsByDept = async (deptId) => {
		if (!deptId) {
			allHostOptions.value = [];
			return;
		}
		try {
			// 带着选中的 deptId 去请求后端修改后的新接口
			const userRes = await request({ 
				url: `/api/v1/users/options?deptId=${deptId}`, 
				method: 'GET' 
			});
			if (userRes && userRes.data) {
				allHostOptions.value = userRes.data.map(item => ({ id: item.value, name: item.label }));
			} else {
				allHostOptions.value = [];
			}
		} catch (err) {
			console.error('根据部门加载人员数据失败', err);
			allHostOptions.value = [];
		}
	};

	onHide(() => {
		if (isAssistMode.value) {
			uni.removeStorageSync('isAssistMode');
			isAssistMode.value = false;
			formData.value.visitDepartment = '';
			formData.value.hostName = '';
			formData.value.deptId = '';
			formData.value.visitedPersonId = '';
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
		deptId: '',            
		visitDepartment: '',   
		visitedPersonId: '',   
		hostName: '',          
		reason: ''
	});

	const formData = ref(getInitialData());
	const companionNames = ref([]);

	const onDateChange = (e) => {
		const selectedDate = e.detail.value;
		if (systemHolidays.value.includes(selectedDate)) {
			uni.showModal({
				title: '日期不可选',
				content: `管理员已将 ${selectedDate} 设为休息日，暂不开放来访预约，请重新选择日期。`,
				showCancel: false,
				confirmText: '我知道了'
			});
			formData.value.visitDate = '';
		} else {
			formData.value.visitDate = selectedDate;
		}
	};

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

	// ======== 部门下拉逻辑 ========
	const showDeptDropdown = ref(false);
	const deptOptions = ref([]);
	let deptTimer = null;

	const fetchDepartments = (keyword = '') => {
		if (keyword) {
			deptOptions.value = allDeptOptions.value.filter(item => item.name.includes(keyword));
		} else {
			deptOptions.value = allDeptOptions.value;
		}
	};

	const onSearchDepartment = (e) => {
		if (isAssistMode.value) return;
		// 一旦手动输入修改部门，要把之前的关联人员全清空
		formData.value.deptId = ''; 
		formData.value.visitedPersonId = '';
		formData.value.hostName = '';
		allHostOptions.value = [];
		
		const keyword = e.detail.value;
		clearTimeout(deptTimer);
		deptTimer = setTimeout(() => {
			fetchDepartments(keyword);
			showDeptDropdown.value = true;
		}, 300);
	};

	const onDeptFocus = () => {
		if (isAssistMode.value) return;
		showDeptDropdown.value = true;
		fetchDepartments(formData.value.visitDepartment);
	};

	const selectDepartment = (item) => {
		formData.value.deptId = item.id;          
		formData.value.visitDepartment = item.name; 
		showDeptDropdown.value = false;
		
		// 💡 联动点：选好部门，清空上一个人的值，并立刻去后端查这批新的人
		formData.value.visitedPersonId = '';
		formData.value.hostName = '';
		loadHostOptionsByDept(item.id);
	};

	// ======== 人员下拉逻辑 ========
	const showHostDropdown = ref(false);
	const hostOptions = ref([]);
	let hostTimer = null;

	const fetchHosts = (keyword = '') => {
		if (keyword) {
			hostOptions.value = allHostOptions.value.filter(item => item.name.includes(keyword));
		} else {
			hostOptions.value = allHostOptions.value;
		}
	};

	const onSearchHost = (e) => {
		if (isAssistMode.value) return;
		if (!formData.value.deptId) return;
		formData.value.visitedPersonId = '';
		const keyword = e.detail.value;
		clearTimeout(hostTimer);
		hostTimer = setTimeout(() => {
			fetchHosts(keyword);
			showHostDropdown.value = true;
		}, 300);
	};

	const onHostFocus = () => {
		if (isAssistMode.value) return;
		
		// 💡 强拦截逻辑：如果不选部门，直接阻止聚焦，并弹窗提示
		if (!formData.value.deptId) {
			uni.showToast({
				title: '请先选择到访单位(部门)',
				icon: 'none'
			});
			// 利用宿主失焦关闭
			uni.hideKeyboard();
			return;
		}
		
		showHostDropdown.value = true;
		fetchHosts(formData.value.hostName);
	};

	const selectHost = (item) => {
		formData.value.visitedPersonId = item.id; 
		formData.value.hostName = item.name;      
		showHostDropdown.value = false;
	};

	const hideDropdown = (type) => {
		setTimeout(() => {
			if (type === 'dept') showDeptDropdown.value = false;
			if (type === 'host') showHostDropdown.value = false;
		}, 200);
	};

	const submitApply = async () => {
		const requiredFields = [
			{ key: 'visitDate', msg: '请选择来访日期' },
			{ key: 'visitTime', msg: '请选择来访时间' },
			{ key: 'applicantName', msg: '请输入申请人姓名' },
			{ key: 'applicantPhone', msg: '请输入申请人电话' },
			{ key: 'applicantIdCard', msg: '请输入申请人身份证号' },
			{ key: 'applicantGender', msg: '请选择性别' },
			{ key: 'applicantCompany', msg: '请输入申请人单位' },
			{ key: 'deptId', msg: '请选择到访单位' },
			{ key: 'visitedPersonId', msg: '请选择被访人' },
			{ key: 'reason', msg: '请输入来访事由' }
		];

		for (let field of requiredFields) {
			if (!formData.value[field.key]) return uni.showToast({
				title: field.msg,
				icon: 'none'
			});
		}

		// 组装最终提交给后端的数据，确保字段名与后端的 VisitorApplicationForm 完全一致
		const submitData = {
		    visitDate: formData.value.visitDate,
		    visitTime: formData.value.visitTime,
		    applicantName: formData.value.applicantName,
		    applicantPhone: formData.value.applicantPhone,
		    visitorCompany: formData.value.applicantCompany, // 💡 映射：前端的单位 对应 后端的 visitorCompany
		    visitorCount: formData.value.visitorCount,
		    visitPurpose: formData.value.reason,             // 💡 映射：前端的事由 对应 后端的 visitPurpose
		    deptId: formData.value.deptId,
		    visitedPersonId: formData.value.visitedPersonId,
		    companionVisitors: companionNames.value.filter(name => name.trim() !== '').join(',')
		};

		try {
			uni.showLoading({ title: '提交中...' });
			const res = await request({
				url: '/api/v1/visitor-applications', 
				method: 'POST',
				data: submitData
			});
			
			uni.hideLoading();
			uni.showToast({
				title: '申请已提交',
				icon: 'success',
				duration: 2000
			});

			setTimeout(() => {
				if (isAssistMode.value) {
					const currentHost = formData.value.hostName;
					const currentDept = formData.value.visitDepartment;
					const currentHostId = formData.value.visitedPersonId;
					const currentDeptId = formData.value.deptId;
					
					formData.value = getInitialData();
					formData.value.hostName = currentHost;
					formData.value.visitDepartment = currentDept;
					formData.value.visitedPersonId = currentHostId;
					formData.value.deptId = currentDeptId;
				} else {
					formData.value = getInitialData();
				}
				companionNames.value = [];
			}, 1000);
			
		} catch (err) {
			uni.hideLoading();
			console.error('提交申请失败:', err);
		}
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
		color: #aaa !important;
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