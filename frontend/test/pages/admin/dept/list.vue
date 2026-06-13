<template>
  <view class="container">
    <view class="header-bar">
      <text class="title">组织架构与部门管理</text>
      <button class="add-btn" size="mini" @click="openModal()">+ 新增部门</button>
    </view>

    <scroll-view scroll-y class="list-area">
      <view v-if="deptList.length === 0" class="empty">暂无部门数据</view>
      
      <view class="card" v-for="item in deptList" :key="item.id">
        <view class="card-left">
          <text class="dept-name">{{ item.name }}</text>
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
        
        <view class="form-group">
          <text class="label">部门名称</text>
          <input class="input" v-model="form.name" placeholder="请输入部门名称" />
        </view>
        
        <view class="form-group">
          <text class="label">显示排序</text>
          <input class="input" type="number" v-model.number="form.sort" placeholder="数字越小越靠前" />
        </view>

        <view class="form-group">
          <text class="label">部门状态</text>
          <switch :checked="form.status === 1" @change="e => form.status = e.detail.value ? 1 : 0" color="#007aff" />
        </view>

        <view class="modal-footer">
          <button class="m-btn cancel" @click="showModal = false">取消</button>
          <button class="m-btn confirm" @click="submitForm">确定</button>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const deptList = ref([]);
const showModal = ref(false);
const form = ref({ id: null, name: '', sort: 1, status: 1 });

// 初始化加载数据
onMounted(() => {
  loadData();
});

// 加载部门列表 (对接 youlai-boot 的 GET /api/v1/dept)
const loadData = () => {
  uni.showLoading({ title: '加载中' });
  // 【API替换点】这里替换为 uni.request 请求后端
  setTimeout(() => {
    deptList.value = [
      { id: 1, name: '集团总部', sort: 1, status: 1 },
      { id: 2, name: '技术研发中心', sort: 2, status: 1 },
      { id: 3, name: '市场运营部', sort: 3, status: 1 },
      { id: 4, name: '行政人事部', sort: 4, status: 1 },
      { id: 5, name: '已裁撤部门', sort: 99, status: 0 }
    ];
    uni.hideLoading();
  }, 500);
};

// 打开新增/编辑弹窗
const openModal = (row = null) => {
  if (row) {
    form.value = { ...row }; // 编辑时回显数据
  } else {
    form.value = { id: null, name: '', sort: 1, status: 1 }; // 新增时清空
  }
  showModal.value = true;
};

// 提交表单 (对接 POST /api/v1/dept 或 PUT /api/v1/dept/{id})
const submitForm = () => {
  if (!form.value.name) return uni.showToast({ title: '部门名称必填', icon: 'none' });
  
  // 【API替换点】这里发送请求给后端
  if (form.value.id) {
    // 编辑逻辑
    const index = deptList.value.findIndex(d => d.id === form.value.id);
    if(index > -1) deptList.value[index] = { ...form.value };
    uni.showToast({ title: '修改成功', icon: 'success' });
  } else {
    // 新增逻辑
    form.value.id = Date.now();
    deptList.value.push({ ...form.value });
    uni.showToast({ title: '新增成功', icon: 'success' });
  }
  
  showModal.value = false;
};

// 删除部门 (对接 DELETE /api/v1/dept/{ids})
const handleDelete = (id) => {
  uni.showModal({
    title: '危险操作',
    content: '确定要删除该部门吗？(需先确保该部门下无员工)',
    success: (res) => {
      if (res.confirm) {
        // 【API替换点】发送删除请求
        deptList.value = deptList.value.filter(d => d.id !== id);
        uni.showToast({ title: '删除成功' });
      }
    }
  });
};
</script>

<style scoped>
.container { height: 100vh; background-color: #f5f7fa; display: flex; flex-direction: column; }
.header-bar { display: flex; justify-content: space-between; align-items: center; padding: 30rpx; background-color: #fff; border-bottom: 1rpx solid #eee; }
.title { font-size: 32rpx; font-weight: bold; color: #333; }
.add-btn { background-color: #007aff; color: #fff; margin: 0; }
.list-area { flex: 1; padding: 20rpx; }
.empty { text-align: center; margin-top: 100rpx; color: #999; }
.card { display: flex; justify-content: space-between; align-items: center; background-color: #fff; padding: 30rpx; border-radius: 12rpx; margin-bottom: 20rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.02); }
.dept-name { font-size: 30rpx; color: #333; font-weight: 500; margin-right: 20rpx; }
.dept-status { font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 6rpx; }
.status-on { background-color: #e6f7ff; color: #1890ff; }
.status-off { background-color: #fff1f0; color: #f5222d; }
.card-right { display: flex; gap: 30rpx; }
.action-btn { font-size: 26rpx; }
.edit { color: #007aff; }
.delete { color: #f5222d; }

/* 弹窗样式 */
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.modal-box { width: 600rpx; background: #fff; border-radius: 20rpx; padding: 40rpx; }
.modal-header { font-size: 34rpx; font-weight: bold; text-align: center; margin-bottom: 40rpx; }
.form-group { display: flex; align-items: center; margin-bottom: 30rpx; }
.label { width: 140rpx; font-size: 28rpx; color: #333; }
.input { flex: 1; height: 70rpx; border: 1px solid #ddd; border-radius: 8rpx; padding: 0 20rpx; font-size: 28rpx; }
.modal-footer { display: flex; justify-content: space-between; margin-top: 50rpx; }
.m-btn { width: 45%; height: 80rpx; line-height: 80rpx; border-radius: 40rpx; font-size: 30rpx; }
.cancel { background-color: #f5f5f5; color: #666; }
.confirm { background-color: #007aff; color: #fff; }
</style>