<template>
	<view class="container">
		<view class="top-bar">
			<picker class="filter-picker" mode="selector" :range="searchDeptOptions" range-key="name"
				@change="onSearchDeptChange">
				<view class="picker-text">
					{{ searchDeptName }} <text class="arrow">▼</text>
				</view>
			</picker>

			<view class="search-box">
				<input class="search-input" v-model="searchKey" placeholder="搜姓名/手机" @confirm="loadData" />
			</view>
			<button class="add-btn" size="mini" @click="openModal()">添加</button>
		</view>

		<scroll-view scroll-y class="list-area">
			<view v-if="userList.length === 0" class="empty">未找到相关员工</view>

			<view class="card" v-for="item in userList" :key="item.id">
				<view class="card-header">
					<view class="user-main">
						<text class="nickname">{{ item.nickname }}</text>
						<text class="username">(@{{ item.username }})</text>
					</view>
					<text class="status-tag" :class="item.status === 1 ? 'status-on' : 'status-off'">
						{{ item.status === 1 ? '在职' : '禁用' }}
					</text>
				</view>

				<view class="card-body">
					<view class="info-row"><text class="label">手机号码：</text>{{ item.mobile }}</view>
					<view class="info-row"><text class="label">所属部门：</text>{{ item.deptName }}</view>
				</view>

				<view class="card-footer">
					<text class="action-btn edit" @click="openModal(item)">编辑</text>
					<text class="action-btn delete" @click="handleDelete(item.id)">删除</text>
				</view>
			</view>
		</scroll-view>

		<view class="modal-mask" v-if="showModal">
			<view class="modal-box">
				<view class="modal-header">{{ form.id ? '编辑员工' : '新增员工' }}</view>
				<scroll-view scroll-y style="max-height: 60vh;">
					<view class="form-group">
						<text class="label"><text style="color:red">*</text>登录账号</text>
						<input class="input" v-model="form.username" placeholder="建议使用拼音或工号" :disabled="!!form.id" />
					</view>
					<view class="form-group">
						<text class="label"><text style="color:red">*</text>员工姓名</text>
						<input class="input" v-model="form.nickname" placeholder="真实姓名" />
					</view>
					<view class="form-group">
						<text class="label"><text style="color:red">*</text>手机号码</text>
						<input class="input" type="number" maxlength="11" v-model="form.mobile" placeholder="11位手机号" />
					</view>
					<view class="form-group">
						<text class="label">所属部门</text>
						<picker class="input picker-box" mode="selector" :range="deptOptions" range-key="name"
							@change="onFormDeptChange">
							<view>{{ form.deptName || '请选择部门' }}</view>
						</picker>
					</view>
					<view class="form-group">
						<text class="label">账号状态</text>
						<switch :checked="form.status === 1" @change="e => form.status = e.detail.value ? 1 : 0"
							color="#007aff" />
					</view>
				</scroll-view>

				<view class="modal-footer">
					<button class="m-btn cancel" @click="showModal = false">取消</button>
					<button class="m-btn confirm" @click="submitForm">保存</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		onMounted
	} from 'vue';

	const searchKey = ref('');
	const searchDeptId = ref('');
	const searchDeptName = ref('全部部门');

	const userList = ref([]);
	const showModal = ref(false);

	const form = ref({
		id: null,
		username: '',
		nickname: '',
		mobile: '',
		deptId: null,
		deptName: '',
		status: 1
	});

	// 基础部门数据
	const deptOptions = ref([{
			id: 1,
			name: '集团总部'
		},
		{
			id: 2,
			name: '技术研发中心'
		},
		{
			id: 3,
			name: '市场运营部'
		}
	]);

	// 搜索用的部门数据 (多加一个"全部部门")
	const searchDeptOptions = ref([{
			id: '',
			name: '全部部门'
		},
		...deptOptions.value
	]);

	onMounted(() => {
		loadData();
	});

	// 筛选部门切换
	const onSearchDeptChange = (e) => {
		const index = e.detail.value;
		searchDeptId.value = searchDeptOptions.value[index].id;
		searchDeptName.value = searchDeptOptions.value[index].name;
		loadData(); // 切换后立刻查询
	};

	// 模拟加载 YouLai Boot: GET /api/v1/users/page
	const loadData = () => {
		uni.showLoading({
			title: '搜索中...'
		});
		setTimeout(() => {
			let mockData = [{
					id: 101,
					username: 'admin',
					nickname: '超级管理员',
					mobile: '13800000000',
					deptId: 1,
					deptName: '集团总部',
					status: 1
				},
				{
					id: 102,
					username: 'lizong',
					nickname: '李总',
					mobile: '13800138001',
					deptId: 2,
					deptName: '技术研发中心',
					status: 1
				},
				{
					id: 103,
					username: 'zhangsan',
					nickname: '张三',
					mobile: '13900139000',
					deptId: 3,
					deptName: '市场运营部',
					status: 0
				}
			];

			// 1. 关键字过滤
			if (searchKey.value) {
				mockData = mockData.filter(u => u.nickname.includes(searchKey.value) || u.mobile.includes(
					searchKey.value));
			}
			// 2. 部门过滤
			if (searchDeptId.value !== '') {
				mockData = mockData.filter(u => u.deptId === searchDeptId.value);
			}

			userList.value = mockData;
			uni.hideLoading();
		}, 400);
	};

	// 表单部门切换
	const onFormDeptChange = (e) => {
		const index = e.detail.value;
		form.value.deptId = deptOptions.value[index].id;
		form.value.deptName = deptOptions.value[index].name;
	};

	const openModal = (row = null) => {
		if (row) {
			form.value = {
				...row
			};
		} else {
			form.value = {
				id: null,
				username: '',
				nickname: '',
				mobile: '',
				deptId: null,
				deptName: '',
				status: 1
			};
		}
		showModal.value = true;
	};

	const submitForm = () => {
		if (!form.value.username || !form.value.nickname || !form.value.mobile) {
			return uni.showToast({
				title: '带*号为必填项',
				icon: 'none'
			});
		}
		if (form.value.id) {
			const idx = userList.value.findIndex(u => u.id === form.value.id);
			if (idx > -1) userList.value[idx] = {
				...form.value
			};
			uni.showToast({
				title: '修改成功',
				icon: 'success'
			});
		} else {
			form.value.id = Date.now();
			userList.value.unshift({
				...form.value
			});
			uni.showToast({
				title: '添加成功',
				icon: 'none'
			});
		}
		showModal.value = false;
	};

	const handleDelete = (id) => {
		if (id === 101) return uni.showToast({
			title: '超级管理员不能删除',
			icon: 'none'
		});
		uni.showModal({
			title: '删除确认',
			content: '确定要删除该员工账号吗？',
			success: (res) => {
				if (res.confirm) {
					userList.value = userList.value.filter(u => u.id !== id);
					uni.showToast({
						title: '删除成功'
					});
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

	.top-bar {
		display: flex;
		align-items: center;
		padding: 20rpx 30rpx;
		background-color: #fff;
		border-bottom: 1rpx solid #eee;
	}

	.filter-picker {
		margin-right: 20rpx;
		font-size: 26rpx;
		color: #007aff;
		font-weight: bold;
		width: 160rpx;
	}

	.arrow {
		font-size: 20rpx;
		margin-left: 4rpx;
	}

	.search-box {
		flex: 1;
		background-color: #f5f5f5;
		border-radius: 30rpx;
		padding: 0 20rpx;
		height: 60rpx;
		display: flex;
		align-items: center;
		margin-right: 20rpx;
	}

	.search-input {
		flex: 1;
		font-size: 26rpx;
	}

	.add-btn {
		background-color: #007aff;
		color: #fff;
		margin: 0;
		border-radius: 30rpx;
		padding: 0 20rpx;
	}

	.list-area {
		flex: 1;
		padding: 20rpx;
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

	.card-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
		padding-bottom: 20rpx;
		border-bottom: 1rpx dashed #eee;
	}

	.user-main {
		display: flex;
		align-items: baseline;
	}

	.nickname {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-right: 10rpx;
	}

	.username {
		font-size: 24rpx;
		color: #999;
	}

	.status-tag {
		font-size: 20rpx;
		padding: 4rpx 12rpx;
		border-radius: 6rpx;
	}

	.status-on {
		background-color: #e6f7ff;
		color: #1890ff;
	}

	.status-off {
		background-color: #fff1f0;
		color: #f5222d;
	}

	.card-body .info-row {
		font-size: 26rpx;
		color: #555;
		margin-bottom: 10rpx;
	}

	.card-body .label {
		color: #999;
		display: inline-block;
		width: 140rpx;
	}

	.card-footer {
		display: flex;
		justify-content: flex-end;
		gap: 40rpx;
		margin-top: 20rpx;
	}

	.action-btn {
		font-size: 26rpx;
		font-weight: 500;
	}

	.edit {
		color: #007aff;
	}

	.delete {
		color: #f5222d;
	}

	/* 弹窗样式保持原样 */
	.modal-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 999;
	}

	.modal-box {
		width: 600rpx;
		background: #fff;
		border-radius: 20rpx;
		padding: 40rpx;
	}

	.modal-header {
		font-size: 34rpx;
		font-weight: bold;
		text-align: center;
		margin-bottom: 40rpx;
	}

	.form-group {
		display: flex;
		align-items: center;
		margin-bottom: 30rpx;
	}

	.label {
		width: 150rpx;
		font-size: 28rpx;
		color: #333;
	}

	.input {
		flex: 1;
		height: 70rpx;
		border: 1px solid #ddd;
		border-radius: 8rpx;
		padding: 0 20rpx;
		font-size: 28rpx;
		box-sizing: border-box;
	}

	.picker-box {
		display: flex;
		align-items: center;
		color: #333;
	}

	.modal-footer {
		display: flex;
		justify-content: space-between;
		margin-top: 40rpx;
	}

	.m-btn {
		width: 45%;
		height: 80rpx;
		line-height: 80rpx;
		border-radius: 40rpx;
		font-size: 30rpx;
	}

	.cancel {
		background-color: #f5f5f5;
		color: #666;
	}

	.confirm {
		background-color: #007aff;
		color: #fff;
	}
</style>