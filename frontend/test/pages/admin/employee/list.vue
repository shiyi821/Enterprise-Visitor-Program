<template>
	<view class="container">
		<view class="top-bar">
			<!-- 一级部门筛选 -->
			<picker class="filter-picker" mode="selector" :range="level1Options" range-key="label"
				@change="onLevel1Change">
				<view class="picker-text">{{ level1Name }} <text class="arrow">▼</text></view>
			</picker>

			<!-- 二级部门筛选（有子部门时显示） -->
			<picker v-if="level2Options.length > 0" class="filter-picker" mode="selector" :range="level2Options"
				range-key="label" @change="onLevel2Change">
				<view class="picker-text">{{ level2Name }} <text class="arrow">▼</text></view>
			</picker>

			<view class="search-box">
				<input class="search-input" v-model="searchKey" placeholder="搜姓名/手机" @confirm="loadData" />
			</view>

			<button class="add-btn search-btn" size="mini" @click="loadData">搜索</button>
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
					<view class="info-row"><text class="label">手机号码：</text>{{ item.mobile || '暂无' }}</view>
					<view class="info-row"><text class="label">所属部门：</text>{{ item.deptName || '未分配部门' }}</view>
					<view class="info-row"><text class="label">系统角色：</text>{{ item.roleNames || '未分配角色' }}</view>
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
						<input class="input" v-model="form.username" placeholder="建议拼音或工号" :disabled="!!form.id" />
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
						<text class="label"><text style="color:red">*</text>系统角色</text>
						<picker class="input picker-box" mode="selector" :range="roleOptions" range-key="name"
							@change="onFormRoleChange">
							<view>{{ form.roleNames || '请分配角色' }}</view>
						</picker>
					</view>

					<view class="form-group">
						<text class="label">账号状态</text>
						<switch :checked="form.status === 1" @change="onStatusChange" color="#007aff" />
					</view>

					<view class="form-group" v-if="!form.id">
						<text class="label" style="color: #999; font-size: 24rpx; width: 100%;">提示：新员工初始密码为
							123456</text>
					</view>
				</scroll-view>

				<view class="modal-footer">
					<button v-if="form.id" class="m-btn cancel" style="color: #f5222d; border: 1rpx solid #ffa39e;"
						@click="handleResetPassword">重置密码</button>
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
	import {
		getEmployeePage,
		addEmployee,
		updateEmployee,
		deleteEmployees,
		getDeptOptions,
		resetEmployeePassword,
		getRoleOptions
	} from '@/api/employee.js';

	const searchKey = ref('');
	const userList = ref([]);
	const showModal = ref(false);

	// ========== 两级联动部门筛选变量 ==========
	const rawDeptTree = ref([]); // 原始树形部门数据
	const level1Options = ref([{
		label: '全部部门',
		value: ''
	}]);
	const level2Options = ref([]);
	const level1Name = ref('全部部门');
	const level2Name = ref('全部子部门');
	const selectedDeptId = ref('');

	// ========== 表单相关变量 ==========
	const form = ref({
		id: null,
		username: '',
		nickname: '',
		mobile: '',
		deptId: null,
		deptName: '',
		roleIds: [],
		roleNames: '',
		status: 1
	});

	const deptOptions = ref([]); // 表单内部门选择器（带层级缩进）
	const roleOptions = ref([]);

	onMounted(() => {
		Promise.all([loadDeptOptions(), loadRoleOptions()]).then(() => {
			loadData();
		});
	});

	// 部门树形打平（仅给表单内选择器用，保留层级缩进效果）
	const flattenDepts = (deptList, level = 0) => {
		let result = [];
		let prefix = '';
		for (let i = 0; i < level; i++) prefix += ' ├─ ';

		deptList.forEach(dept => {
			result.push({
				id: dept.value,
				name: prefix + dept.label,
				originalName: dept.label,
				children: dept.children
			});
			if (dept.children && dept.children.length > 0) {
				result = result.concat(flattenDepts(dept.children, level + 1));
			}
		});
		return result;
	};

	const loadDeptOptions = async () => {
		try {
			const res = await getDeptOptions();
			if (res.code === '00000' && res.data) {
				// 1. 保存原始树形数据，给顶部两级联动筛选用
				rawDeptTree.value = res.data;
				level1Options.value = [{
					label: '全部部门',
					value: ''
				}, ...res.data];

				// 2. 打平数据，给表单内部门选择器用（保留层级缩进）
				const flatDepts = flattenDepts(res.data);
				deptOptions.value = flatDepts;
			}
		} catch (error) {
			console.error('获取部门字典失败:', error);
		}
	};

	const loadRoleOptions = async () => {
	    try {
	        const res = await getRoleOptions();
	        if (res.code === '00000' && res.data) {
	            roleOptions.value = res.data
	                .map(item => ({
	                    id: item.value,
	                    name: item.label
	                }))
	                // 过滤掉「访问人」角色，不在下拉选项中显示
	                .filter(item => item.name !== '访问人');
	        }
	    } catch (error) {
	        console.error('获取角色字典失败:', error);
	    }
	};

	// ========== 两级联动筛选逻辑 ==========
	// 切换一级部门
	const onLevel1Change = (e) => {
		const item = level1Options.value[e.detail.value];
		level1Name.value = item.label;
		selectedDeptId.value = item.value;

		// 有子部门则更新二级选项，无则清空二级
		if (item.children && item.children.length > 0) {
			level2Options.value = [{
				label: '全部子部门',
				value: item.value
			}, ...item.children];
			level2Name.value = '全部子部门';
		} else {
			level2Options.value = [];
			level2Name.value = '子部门';
		}
		loadData();
	};

	// 切换二级部门
	const onLevel2Change = (e) => {
		const item = level2Options.value[e.detail.value];
		level2Name.value = item.label;
		selectedDeptId.value = item.value;
		loadData();
	};

	// ========== 列表加载逻辑 ==========
	const loadData = async () => {
		uni.showLoading({
			title: '加载中...'
		});
		try {
			const queryParams = {
				pageNum: 1,
				pageSize: 100,
				keywords: searchKey.value || undefined,
				deptId: selectedDeptId.value === '' ? undefined : selectedDeptId.value
			};

			const res = await getEmployeePage(queryParams);
			if (res.code === '00000') {
				let allUsers = res.data?.list || [];

				// 1. 过滤访客角色
				allUsers = allUsers.filter(user => {
					if (user.roleNames && user.roleNames.includes('访问人')) return false;
					return true;
				});

				// 2. 选中具体二级部门时，前端精准匹配部门名称（解决后台部门ID查询不准的问题）
				// 选"全部子部门"或"全部部门"时，不做前端拦截，由后台返回对应范围数据
				if (level2Options.length > 0 && level2Name.value !== '全部子部门') {
					allUsers = allUsers.filter(user => user.deptName === level2Name.value);
				}

				userList.value = allUsers;
			} else {
				uni.showToast({
					title: res.msg || '获取列表失败',
					icon: 'none'
				});
			}
		} catch (error) {
			console.error('获取员工数据异常:', error);
			uni.showToast({
				title: '网络连接异常',
				icon: 'none'
			});
		} finally {
			uni.hideLoading();
		}
	};

	// ========== 表单操作逻辑 ==========
	const onFormDeptChange = (e) => {
		const index = e.detail.value;
		form.value.deptId = deptOptions.value[index].id;
		form.value.deptName = deptOptions.value[index].originalName;
	};

	const onFormRoleChange = (e) => {
		const index = e.detail.value;
		form.value.roleIds = [roleOptions.value[index].id];
		form.value.roleNames = roleOptions.value[index].name;
	};

	const onStatusChange = (e) => {
		form.value.status = e.detail.value ? 1 : 0;
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
				roleIds: [],
				roleNames: '',
				status: 1
			};
		}
		showModal.value = true;
	};

	const submitForm = async () => {
		if (!form.value.username || !form.value.nickname || !form.value.mobile) {
			return uni.showToast({
				title: '带*号为必填项',
				icon: 'none'
			});
		}
		if (!form.value.roleIds || form.value.roleIds.length === 0) {
			return uni.showToast({
				title: '请为员工分配系统角色',
				icon: 'none'
			});
		}

		uni.showLoading({
			title: '正在保存...'
		});

		try {
			const payload = {
				username: form.value.username,
				nickname: form.value.nickname,
				mobile: form.value.mobile,
				deptId: form.value.deptId,
				roleIds: form.value.roleIds,
				status: form.value.status
			};

			let res;
			if (form.value.id) {
				payload.id = form.value.id;
				res = await updateEmployee(form.value.id, payload);
			} else {
				payload.password = '123456';
				res = await addEmployee(payload);
			}

			if (res.code === '00000') {
				uni.showToast({
					title: form.value.id ? '修改成功' : '添加成功',
					icon: 'success'
				});
				showModal.value = false;
				loadData();
			} else {
				uni.showToast({
					title: res.msg || '保存失败',
					icon: 'none'
				});
			}
		} catch (error) {
			console.error('保存报错:', error);
			uni.showToast({
				title: '请求服务器失败',
				icon: 'none'
			});
		} finally {
			uni.hideLoading();
		}
	};

	const handleResetPassword = () => {
		uni.showModal({
			title: '安全确认',
			content: `确定要将员工【${form.value.nickname}】的密码重置为 123456 吗？`,
			success: async (res) => {
				if (res.confirm) {
					uni.showLoading({
						title: '正在重置...'
					});
					try {
						const resetRes = await resetEmployeePassword(form.value.id, '123456');
						if (resetRes.code === '00000') {
							uni.showToast({
								title: '密码已重置',
								icon: 'success'
							});
							showModal.value = false;
						} else {
							uni.showToast({
								title: resetRes.msg || '重置失败',
								icon: 'none'
							});
						}
					} catch (error) {
						console.error('重置密码报错:', error);
						uni.showToast({
							title: '重置密码失败，请检查网络',
							icon: 'none'
						});
					} finally {
						uni.hideLoading();
					}
				}
			}
		});
	};

	const handleDelete = (id) => {
		if (id === 1 || id === 101) {
			return uni.showToast({
				title: '内置系统管理员不能删除',
				icon: 'none'
			});
		}
		uni.showModal({
			title: '删除确认',
			content: '确定要删除该员工账号吗？',
			success: async (res) => {
				if (res.confirm) {
					uni.showLoading({
						title: '删除中...'
					});
					try {
						const delRes = await deleteEmployees(id);
						if (delRes.code === '00000') {
							uni.showToast({
								title: '删除成功',
								icon: 'success'
							});
							loadData();
						} else {
							uni.showToast({
								title: delRes.msg || '删除失败',
								icon: 'none'
							});
						}
					} catch (error) {
						uni.showToast({
							title: '网络服务异常',
							icon: 'none'
						});
					} finally {
						uni.hideLoading();
					}
				}
			}
		});
	};
</script>

<style scoped>
	:global(page::-webkit-scrollbar),
	::-webkit-scrollbar {
		display: none !important;
		width: 0 !important;
		height: 0 !important;
		color: transparent !important;
		background: transparent !important;
	}


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
		margin-right: 16rpx;
		font-size: 24rpx;
		color: #007aff;
		font-weight: bold;
		max-width: 140rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
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

	.search-btn {
		background-color: #52c41a;
		margin-right: 16rpx;
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
		width: 85%;
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
		align-items: center;
		margin-top: 40rpx;
	}

	.m-btn {
		width: 30%;
		height: 70rpx;
		line-height: 70rpx;
		border-radius: 35rpx;
		font-size: 28rpx;
		padding: 0;
		margin: 0 10rpx;
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