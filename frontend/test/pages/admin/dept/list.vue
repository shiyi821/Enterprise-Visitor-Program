<template>
	<view class="container">
		<view class="header-bar">
			<text class="title">组织架构与部门管理</text>
			<button class="add-btn" size="mini" @click="openModal()">+ 新增部门</button>
		</view>

		<view class="search-bar-row">
			<view class="search-box">
				<input 
					class="search-input" 
					v-model="searchKey" 
					placeholder="输入部门名称或编码查找..." 
					confirm-type="search"
					@confirm="loadData" 
				/>
				<text v-if="searchKey" class="clear-text" @click="handleClearSearch">×</text>
			</view>
			<button class="search-btn" size="mini" @click="loadData">搜索</button>
		</view>

		<scroll-view scroll-y class="list-area">
			<view v-if="visibleDeptList.length === 0" class="empty">未找到相关部门数据</view>
			
			<view class="card" v-for="item in visibleDeptList" :key="item.id">
				
				<view class="card-left" :style="{ paddingLeft: (item.level * 40) + 'rpx' }">
					
					<view class="tree-toggle" @click="toggleExpand(item)">
						<text v-if="item.hasChildren" class="toggle-icon">{{ item.expanded ? '▼' : '▶' }}</text>
						<text v-else class="toggle-icon dot">●</text>
					</view>
					
					<view class="dept-info" @click="toggleExpand(item)">
						<text class="dept-name">{{ item.originalName }}</text>
						<text v-if="item.code" class="dept-code">[{{ item.code }}]</text>
					</view>
					
					<text class="dept-status" :class="item.status === 1 ? 'status-on' : 'status-off'">
						{{ item.status === 1 ? '正常' : '停用' }}
					</text>
				</view>

				<view class="card-right">
					<text class="action-btn edit" @click="openModal(item)">编辑</text>
					<text class="action-btn delete" @click="handleDelete(item.id)">删除</text>
				</view>
			</view>
		</scroll-view>

		<view class="modal-mask" v-if="showModal">
			<view class="modal-box">
				<view class="modal-header">{{ form.id ? '编辑部门' : '新增部门' }}</view>
				<scroll-view scroll-y style="max-height: 60vh;">
					
					<view class="form-group">
						<text class="label"><text style="color:red">*</text>上级部门</text>
						<picker class="input picker-box" mode="selector" :range="parentDeptOptions" range-key="name" @change="onParentDeptChange">
							<view>{{ form.parentName || '请选择上级部门' }}</view>
						</picker>
					</view>

					<view class="form-group">
						<text class="label"><text style="color:red">*</text>部门名称</text>
						<input class="input" v-model="form.name" placeholder="请输入部门名称" />
					</view>
					
					<view class="form-group">
						<text class="label"><text style="color:red">*</text>部门编码</text>
						<input class="input" v-model="form.code" placeholder="如: RD, HR, TECH" />
					</view>
					
					<view class="form-group">
						<text class="label">显示排序</text>
						<input class="input" type="number" v-model="form.sort" placeholder="数字越小越靠前" />
					</view>

					<view class="form-group">
						<text class="label">部门状态</text>
						<switch :checked="form.status === 1" @change="e => form.status = e.detail.value ? 1 : 0" color="#007aff" />
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
	import { ref, computed, onMounted } from 'vue';
	import { getDeptList, getDeptOptions, addDept, updateDept, deleteDept } from '@/api/dept.js';

	// 响应式变量
	const searchKey = ref(''); 
	const deptList = ref([]); // 原始平铺数据
	const showModal = ref(false);
	const parentDeptOptions = ref([]);

	const form = ref({
		id: null,
		parentId: 0,
		parentName: '顶级部门',
		name: '',
		code: '',
		sort: 1,
		status: 1
	});

	onMounted(() => {
		loadData();
		loadDeptOptions();
	});

	// 【核心升级】：在平铺时记录节点关系和初始的展开状态
	const flattenTree = (list, level = 0, parentId = 0) => {
		let result = [];
		list.forEach(item => {
			const hasChildren = item.children && item.children.length > 0;
			result.push({
				...item,
				level: level, 
				originalName: item.name,
				parentId: parentId,
				hasChildren: hasChildren,
				expanded: true // 默认列表全部展开，方便查阅
			});
			// 递归加入子节点
			if (hasChildren) {
				result = result.concat(flattenTree(item.children, level + 1, item.id));
			}
		});
		return result;
	};
	
	// 平铺下拉框选项 (弹窗内保持原状)
	const flattenOptions = (list, level = 0) => {
		let result = [];
		let prefix = '';
		for (let i = 0; i < level; i++) prefix += ' ├─ '; 

		list.forEach(item => {
			result.push({
				id: item.value,
				name: prefix + item.label,
				originalName: item.label
			});
			if (item.children && item.children.length > 0) {
				result = result.concat(flattenOptions(item.children, level + 1));
			}
		});
		return result;
	};

	// 【动态计算属性】：根据节点的展开/折叠状态，实时计算最终在页面上应该显示哪些部门
	const visibleDeptList = computed(() => {
		let result = [];
		let hiddenParents = new Set(); // 记录被折叠的父节点 ID 黑名单
		
		for (let item of deptList.value) {
			// 如果当前节点的父亲在黑名单里，说明父亲被折叠了，自己也必须隐藏
			if (hiddenParents.has(item.parentId)) {
				// 把自己也加入黑名单，防止漏网孙子节点
				hiddenParents.add(item.id); 
				continue;
			}
			
			// 能够执行到这的，都是可见节点
			result.push(item);
			
			// 如果自己被用户主动点击折叠了，将自己加入黑名单，隐藏下面的所有子孙
			if (!item.expanded) {
				hiddenParents.add(item.id);
			}
		}
		return result;
	});

	// 切换折叠状态
	const toggleExpand = (item) => {
		if (item.hasChildren) {
			item.expanded = !item.expanded;
		}
	};

	// 修改后的 loadData 函数
const loadData = async () => {
		uni.showLoading({ title: '检索中...' });
		try {
			// 🚀 修改点：在这里做严格判断
			const queryParams = {};
			if (searchKey.value && searchKey.value.trim() !== '') {
				queryParams.keywords = searchKey.value.trim();
			}

			const res = await getDeptList(queryParams);
			if (res.code === '00000' && res.data) {
				// 依然保留深拷贝，适配小程序
				deptList.value = JSON.parse(JSON.stringify(flattenTree(res.data)));
			} else {
				uni.showToast({ title: res.msg || '获取数据失败', icon: 'none' });
			}
		} catch (error) {
			console.error('获取部门列表异常:', error);
			uni.showToast({ title: '网络连接异常', icon: 'none' });
		} finally {
			uni.hideLoading();
		}
	};
	const handleClearSearch = () => {
		searchKey.value = '';
		loadData();
	};

	const loadDeptOptions = async () => {
		try {
			const res = await getDeptOptions();
			if (res.code === '00000' && res.data) {
				const flatOptions = flattenOptions(res.data);
				// 🚀 核心修改：使用深拷贝去除 Proxy 代理
				parentDeptOptions.value = JSON.parse(JSON.stringify([
					{ id: 0, name: '顶级部门', originalName: '顶级部门' },
					...flatOptions
				]));
			}
		} catch (error) {
			console.error('获取部门字典失败:', error);
		}
	};

	const onParentDeptChange = (e) => {
		const index = e.detail.value;
		const selected = parentDeptOptions.value[index];
		form.value.parentId = selected.id;
		form.value.parentName = selected.originalName;
	};

	const openModal = (row = null) => {
		if (row) {
			let pName = '顶级部门';
			if (row.parentId !== 0) {
				const pDept = parentDeptOptions.value.find(d => d.id === row.parentId);
				if (pDept) pName = pDept.originalName;
			}
			
			form.value = {
				id: row.id,
				parentId: row.parentId || 0,
				parentName: pName,
				name: row.originalName || row.name,
				code: row.code || '', 
				sort: row.sort || 1,
				status: row.status !== undefined ? row.status : 1
			};
		} else {
			form.value = {
				id: null,
				parentId: 0,
				parentName: '顶级部门',
				name: '',
				code: '', 
				sort: 1,
				status: 1
			};
		}
		showModal.value = true;
	};

	const submitForm = async () => {
		if (!form.value.name) return uni.showToast({ title: '部门名称不能为空', icon: 'none' });
		if (!form.value.code) return uni.showToast({ title: '部门编码不能为空', icon: 'none' });
		if (form.value.parentId === form.value.id && form.value.id != null) {
			return uni.showToast({ title: '上级部门不能是自己', icon: 'none' });
		}

		uni.showLoading({ title: '正在保存...' });
		try {
			const payload = {
				parentId: form.value.parentId,
				name: form.value.name,
				code: form.value.code, 
				sort: parseInt(form.value.sort) || 1,
				status: form.value.status
			};

			let res;
			if (form.value.id) {
				payload.id = form.value.id;
				res = await updateDept(form.value.id, payload);
			} else {
				res = await addDept(payload);
			}

			if (res.code === '00000') {
				uni.showToast({ title: form.value.id ? '修改成功' : '添加成功', icon: 'success' });
				showModal.value = false;
				loadData();
				loadDeptOptions();
			} else {
				uni.showToast({ title: res.msg || '保存失败', icon: 'none' });
			}
		} catch (error) {
			console.error('保存异常:', error);
			uni.showToast({ title: '请求服务器失败', icon: 'none' });
		} finally {
			uni.hideLoading();
		}
	};

	const handleDelete = (id) => {
		uni.showModal({
			title: '删除确认',
			content: '确定要删除该部门吗？',
			success: async (res) => {
				if (res.confirm) {
					uni.showLoading({ title: '删除中...' });
					try {
						const delRes = await deleteDept(id);
						if (delRes.code === '00000') {
							uni.showToast({ title: '删除成功', icon: 'success' });
							loadData();
							loadDeptOptions();
						} else {
							uni.showToast({ title: delRes.msg || '删除失败', icon: 'none' });
						}
					} catch (error) {
						uni.showToast({ title: '网络异常', icon: 'none' });
					} finally {
						uni.hideLoading();
					}
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

	.header-bar {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 30rpx 30rpx 15rpx 30rpx;
		background-color: #fff;
	}

	.title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		border-left: 8rpx solid #007aff;
		padding-left: 16rpx;
	}

	.add-btn {
		background-color: #007aff;
		color: #fff;
		margin: 0;
		border-radius: 30rpx;
		padding: 0 30rpx;
		font-size: 26rpx;
	}

	.search-bar-row {
		display: flex;
		align-items: center;
		padding: 15rpx 30rpx 25rpx 30rpx;
		background-color: #fff;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.search-box {
		flex: 1;
		background-color: #f4f5f7;
		border-radius: 30rpx;
		padding: 0 24rpx;
		height: 64rpx;
		display: flex;
		align-items: center;
		position: relative;
	}

	.search-input {
		flex: 1;
		font-size: 26rpx;
		color: #333;
	}

	.clear-text {
		font-size: 36rpx;
		color: #999;
		padding: 0 10rpx;
		cursor: pointer;
	}

	.search-btn {
		background-color: #007aff;
		color: #fff;
		margin: 0 0 0 20rpx;
		border-radius: 30rpx;
		padding: 0 32rpx;
		font-size: 26rpx;
		height: 64rpx;
		line-height: 64rpx;
	}

	.list-area {
		flex: 1;
		padding: 20rpx;
		box-sizing: border-box;
	}

	.empty {
		text-align: center;
		margin-top: 150rpx;
		color: #999;
		font-size: 28rpx;
	}

	.card {
		display: flex;
		justify-content: space-between;
		align-items: center;
		background-color: #fff;
		padding: 30rpx;
		border-radius: 12rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.02);
	}

	.card-left {
		display: flex;
		align-items: center;
		transition: padding-left 0.2s ease; 
	}
	
	/* 【样式升级】：展开/折叠三角按钮样式 */
	.tree-toggle {
		width: 40rpx;
		height: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 10rpx;
	}
	
	.toggle-icon {
		font-size: 20rpx;
		color: #8c8c8c;
		font-weight: bold;
	}
	
	.dot {
		font-size: 16rpx;
		color: #d9d9d9;
	}

	.dept-info {
		display: flex;
		align-items: center;
		margin-right: 20rpx;
	}

	.dept-name {
		font-size: 29rpx;
		color: #333;
		font-weight: 500;
	}
	
	.dept-code {
		font-size: 24rpx;
		color: #1890ff;
		margin-left: 12rpx;
		font-weight: bold;
		background-color: rgba(24, 144, 255, 0.08);
		padding: 2rpx 10rpx;
		border-radius: 6rpx;
	}

	.dept-status {
		font-size: 20rpx;
		padding: 4rpx 12rpx;
		border-radius: 6rpx;
		margin-left: 10rpx;
	}

	.status-on {
		background-color: #e6f7ff;
		color: #1890ff;
		border: 1rpx solid #91d5ff;
	}

	.status-off {
		background-color: #fff1f0;
		color: #f5222d;
		border: 1rpx solid #ffa39e;
	}

	.card-right {
		display: flex;
		gap: 30rpx;
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
		top: 0; left: 0; right: 0; bottom: 0;
		background: rgba(0, 0, 0, 0.5);
		display: flex; align-items: center; justify-content: center;
		z-index: 999;
	}

	.modal-box {
		width: 80%;
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
		width: 140rpx;
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
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.modal-footer {
		display: flex;
		justify-content: space-between;
		margin-top: 40rpx;
	}

	.m-btn {
		width: 45%;
		height: 70rpx;
		line-height: 70rpx;
		border-radius: 35rpx;
		font-size: 28rpx;
		padding: 0;
		margin: 0;
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