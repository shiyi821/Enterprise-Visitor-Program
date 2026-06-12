<template>
  <view class="container">
    <view class="form-group">
      <text class="label"><text class="required">*</text>申请来访日期</text>
      <picker mode="date" @change="(e) => formData.visitDate = e.detail.value">
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
      <input class="input" type="number" maxlength="11" placeholder="请输入11位手机号" v-model="formData.applicantPhone" />
    </view>

    <view class="form-group">
      <text class="label"><text class="required">*</text>申请人身份证号</text>
      <input class="input" type="text" maxlength="18" placeholder="请输入18位身份证号码" v-model="formData.applicantIdCard" />
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
        <input class="step-input" type="number" v-model.number="formData.visitorCount" @blur="validateVisitorCount" />
        <view class="step-btn" @click="changeVisitorCount(1)">+</view>
      </view>
    </view>

    <view class="form-group" v-if="formData.visitorCount > 1">
      <text class="label">同行来访人姓名 (选填)</text>
      <view class="companion-grid">
        <input 
          class="input companion-input" 
          v-for="(item, index) in companionNames" 
          :key="index" 
          v-model="companionNames[index]" 
          :placeholder="'同行人 ' + (index + 1)" 
        />
      </view>
    </view>

    <view class="form-group search-group">
      <text class="label"><text class="required">*</text>到访单位</text>
      <input 
        class="input" 
        placeholder="点击查看全部或输入搜索" 
        v-model="formData.visitDepartment" 
        @input="onSearchDepartment" 
        @focus="onDeptFocus"
        @blur="hideDropdown('dept')"
      />
      <scroll-view scroll-y class="dropdown-list" v-if="showDeptDropdown && deptOptions.length > 0">
        <view class="dropdown-item" v-for="item in deptOptions" :key="item.id" @click="selectDepartment(item)">
          {{ item.name }}
        </view>
      </scroll-view>
    </view>

    <view class="form-group search-group">
      <text class="label"><text class="required">*</text>被访人</text>
      <input 
        class="input" 
        placeholder="点击查看全部或输入搜索" 
        v-model="formData.hostName" 
        @input="onSearchHost" 
        @focus="onHostFocus"
        @blur="hideDropdown('host')"
      />
      <scroll-view scroll-y class="dropdown-list" v-if="showHostDropdown && hostOptions.length > 0">
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

// --- 表单初始状态维护 ---
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

// --- 人数及同行人逻辑 ---
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

// --- 下拉搜索逻辑 (到访单位) ---
const showDeptDropdown = ref(false);
const deptOptions = ref([]);
let deptTimer = null;

const fetchDepartments = (keyword = '') => {
  // 【对接后端提示】在此处请求后端接口，传参 keyword
  if (keyword) {
    deptOptions.value = [
      { id: 11, name: `${keyword}技术部` },
      { id: 12, name: `${keyword}业务部` }
    ];
  } else {
    // 默认列表
    deptOptions.value = [
      { id: 1, name: '集团总部' },
      { id: 2, name: '技术研发中心' },
      { id: 3, name: '市场运营部' },
      { id: 4, name: '行政人事部' }
    ];
  }
};

const onSearchDepartment = (e) => {
  const keyword = e.detail.value;
  clearTimeout(deptTimer);
  deptTimer = setTimeout(() => {
    fetchDepartments(keyword);
  }, 300);
};

const onDeptFocus = () => {
  showDeptDropdown.value = true;
  fetchDepartments(formData.value.visitDepartment);
};

const selectDepartment = (item) => {
  formData.value.visitDepartment = item.name;
  showDeptDropdown.value = false;
};

// --- 下拉搜索逻辑 (被访人) ---
const showHostDropdown = ref(false);
const hostOptions = ref([]);
let hostTimer = null;

const fetchHosts = (keyword = '') => {
  // 【对接后端提示】在此处请求后端接口，传参 keyword
  if (keyword) {
    hostOptions.value = [
      { id: 11, name: `${keyword}总监` },
      { id: 12, name: `${keyword}经理` }
    ];
  } else {
    // 默认列表
    hostOptions.value = [
      { id: 1, name: '张三 (前台接待)' },
      { id: 2, name: '李四 (行政经理)' },
      { id: 3, name: '王五 (人事专员)' }
    ];
  }
};

const onSearchHost = (e) => {
  const keyword = e.detail.value;
  clearTimeout(hostTimer);
  hostTimer = setTimeout(() => {
    fetchHosts(keyword);
  }, 300);
};

const onHostFocus = () => {
  showHostDropdown.value = true;
  fetchHosts(formData.value.hostName);
};

const selectHost = (item) => {
  formData.value.hostName = item.name;
  showHostDropdown.value = false;
};

// --- 下拉框失去焦点的通用隐藏 ---
const hideDropdown = (type) => {
  setTimeout(() => {
    if (type === 'dept') showDeptDropdown.value = false;
    if (type === 'host') showHostDropdown.value = false;
  }, 200);
};

// --- 提交与校验逻辑 ---
const submitApply = () => {
  const requiredFields = [
    { key: 'visitDate', msg: '请选择来访日期' },
    { key: 'visitTime', msg: '请选择来访时间' },
    { key: 'applicantName', msg: '请输入申请人姓名' },
    { key: 'applicantPhone', msg: '请输入申请人电话' },
    { key: 'applicantIdCard', msg: '请输入申请人身份证号' },
    { key: 'applicantGender', msg: '请选择性别' },
    { key: 'applicantCompany', msg: '请输入申请人单位' },
    { key: 'visitDepartment', msg: '请选择到访单位' },
    { key: 'hostName', msg: '请选择被访人' },
    { key: 'reason', msg: '请输入来访事由' }
  ];

  for (let field of requiredFields) {
    if (!formData.value[field.key]) {
      return uni.showToast({ title: field.msg, icon: 'none' });
    }
  }

  // 电话校验 (11位数字)
  const phoneReg = /^1[3-9]\d{9}$/;
  if (!phoneReg.test(formData.value.applicantPhone)) {
    return uni.showToast({ title: '手机号格式不正确', icon: 'none' });
  }

  // 身份证号校验 (使用要求的正则)
  const idCardReg = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/;
  if (!idCardReg.test(formData.value.applicantIdCard)) {
    return uni.showToast({ title: '身份证号格式不正确', icon: 'none' });
  }

  // 组装同行人
  const companionsStr = companionNames.value.filter(name => name.trim() !== '').join(',');

  const submitData = {
    ...formData.value,
    companions: companionsStr
  };

  console.log('提交给后端的数据：', submitData);

  // 【对接后端提示】在此调用提交接口
  // api.submit(submitData).then(() => { ... })
  
  uni.showToast({
    title: '申请已提交',
    icon: 'success',
    duration: 2000
  });

  // 延迟清空本页内容，让用户看到成功提示
  setTimeout(() => {
    formData.value = getInitialData();
    companionNames.value = [];
  }, 1000);
};
</script>

<style lang="scss">
.container {
  padding: 20px;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.form-group {
  margin-bottom: 20px;
  
  .label {
    display: block;
    margin-bottom: 8px;
    font-size: 14px;
    color: #333;
    font-weight: 500;
  }
  
  .required {
    color: #ff4d4f;
    margin-right: 4px;
  }
  
  .input {
    height: 44px;
    border: 1px solid #e5e5e5;
    border-radius: 6px;
    padding: 0 12px;
    background-color: #fff;
    font-size: 14px;
    box-sizing: border-box;
    width: 100%;
  }

  .textarea {
    height: 80px;
    padding: 12px;
  }
  
  .picker-text {
    line-height: 44px;
    color: #333;
  }
}

.radio-group {
  display: flex;
  align-items: center;
  height: 44px;
  
  .radio-label {
    margin-right: 30px;
    display: flex;
    align-items: center;
    font-size: 14px;
    
    radio {
      transform: scale(0.8);
    }
  }
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

  .step-btn {
    width: 36px;
    height: 100%;
    display: flex;
    justify-items: center;
    align-items: center;
    justify-content: center;
    background-color: #f5f5f5;
    font-size: 18px;
    color: #666;
    
    &:active {
      background-color: #e0e0e0;
    }
  }

  .step-input {
    flex: 1;
    height: 100%;
    text-align: center;
    font-size: 14px;
    border-left: 1px solid #e5e5e5;
    border-right: 1px solid #e5e5e5;
  }
}

.companion-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  
  .companion-input {
    width: calc(33.33% - 6.66px);
    text-align: center;
    padding: 0 4px;
  }
}

.search-group {
  position: relative;
  
  .dropdown-list {
    position: absolute;
    top: 75px;
    left: 0;
    width: 100%;
    max-height: 200px;
    background-color: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 6px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    z-index: 10;
    
    .dropdown-item {
      padding: 12px;
      font-size: 14px;
      color: #333;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:active {
        background-color: #f8f9fa;
      }
    }
  }
}

.submit-btn {
  margin-top: 40px;
  margin-bottom: 40px;
  background-color: #245381;
  color: #fff;
  border-radius: 6px;
  font-size: 16px;
  
  &::after {
    border: none;
  }
  
  &:active {
    opacity: 0.9;
  }
}
</style>