<template>
  <view class="admin-container">
    <view class="header">
      <text class="title">星瀚控制台</text>
      <text class="subtitle">管理员：{{ userInfo.username || '-' }}</text>
    </view>

    <view class="ops-row">
      <button class="btn" @click="loadSourceList">刷新源列表</button>
      <button class="btn btn-primary" @click="syncVideo">立即同步视频</button>
      <button class="btn btn-warn" @click="logout">退出</button>
    </view>

    <view class="panel">
      <text class="panel-title">源 API 管理（增删改查）</text>
      <view class="tip">API格式示例：https://caiji.xxx.com/api.php/provide/vod/at/json</view>

      <view class="form-grid">
        <input class="input" v-model="form.sourceName" placeholder="API名字（如：茅台m3u8）" placeholder-class="ph" />
        <input class="input" v-model="form.apiUrl" placeholder="API地址（必填）" placeholder-class="ph" />
        <input class="input" v-model="form.domainUrl" placeholder="域名（如：https://caiji.xxx.com）" placeholder-class="ph" />
      </view>
      <view class="ops-row">
        <button class="btn btn-primary" @click="saveSource">新增源</button>
        <button class="btn" @click="clearForm">清空表单</button>
      </view>

      <view class="source-list">
        <view class="source-item" v-for="item in sourceList" :key="item.id">
          <view class="left">
            <text class="name">{{ item.sourceName }}</text>
            <text class="url">{{ item.apiUrl }}</text>
            <text class="url">域名：{{ item.domainUrl }}</text>
          </view>
          <view class="right">
            <text class="status" :class="{ active: item.status === 1 }">{{ item.status === 1 ? '启用' : '禁用' }}</text>
            <button class="small" @click="fillForm(item)">编辑</button>
            <button class="small" @click="toggleStatus(item)">{{ item.status === 1 ? '禁用' : '启用' }}</button>
            <button class="small danger" @click="deleteSource(item.id)">删除</button>
          </view>
        </view>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">广告管理（基础）</text>
      <view class="ops-row">
        <button class="btn" @click="loadAdList">刷新广告</button>
      </view>
      <view class="ad-item" v-for="ad in adList" :key="ad.id">
        <text>{{ ad.title || '未命名广告' }}</text>
      </view>
      <text class="tip" v-if="adList.length === 0">暂无广告数据</text>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import request from '@/utils/request.js';

const userInfo = ref({});
const sourceList = ref([]);
const adList = ref([]);
const form = reactive({
  id: null,
  sourceName: '',
  apiUrl: '',
  domainUrl: ''
});

const ensureAdmin = () => {
  const localUser = uni.getStorageSync('userInfo') || {};
  userInfo.value = localUser;
  if (!localUser?.isAdmin) {
    uni.showToast({ title: '无权限访问', icon: 'none' });
    setTimeout(() => uni.reLaunch({ url: '/pages/index/index' }), 800);
    return false;
  }
  return true;
};

onLoad(async () => {
  if (!ensureAdmin()) return;
  await Promise.all([loadSourceList(), loadAdList()]);
});

const loadSourceList = async () => {
  try {
    const res = await request({ url: '/admin/source/list', data: { page: 1, size: 50 } });
    sourceList.value = res?.records || [];
  } catch (e) {
    sourceList.value = [];
  }
};

const saveSource = async () => {
  if (!form.sourceName.trim()) {
    uni.showToast({ title: '请填写API名字', icon: 'none' });
    return;
  }
  if (!/^https?:\/\/.+/i.test(form.apiUrl) || !form.apiUrl.includes('/api.php/provide/vod')) {
    uni.showToast({ title: 'API地址格式不正确', icon: 'none' });
    return;
  }

  if (form.id) {
    await request({ url: '/admin/source/update', method: 'PUT', data: { ...form, status: 1 } });
    uni.showToast({ title: '修改成功', icon: 'none' });
  } else {
    await request({ url: '/admin/source/save', method: 'POST', data: { ...form, status: 1 } });
    uni.showToast({ title: '新增成功', icon: 'none' });
  }

  clearForm();
  await loadSourceList();
};

const fillForm = (item) => {
  form.id = item.id;
  form.sourceName = item.sourceName;
  form.apiUrl = item.apiUrl;
  form.domainUrl = item.domainUrl;
};

const clearForm = () => {
  form.id = null;
  form.sourceName = '';
  form.apiUrl = '';
  form.domainUrl = '';
};

const deleteSource = async (id) => {
  await request({ url: `/admin/source/delete/${id}`, method: 'DELETE' });
  uni.showToast({ title: '删除成功', icon: 'none' });
  await loadSourceList();
};

const toggleStatus = async (item) => {
  const next = item.status === 1 ? 0 : 1;
  await request({ url: `/admin/source/status/${item.id}/${next}`, method: 'PUT' });
  await loadSourceList();
};

const syncVideo = async () => {
  uni.showLoading({ title: '同步中', mask: true });
  try {
    await request({ url: '/api/sync/video', method: 'POST' });
    uni.hideLoading();
    uni.showToast({ title: '已触发同步', icon: 'none' });
  } catch (e) {
    uni.hideLoading();
  }
};

const loadAdList = async () => {
  try {
    const res = await request({ url: '/api/ad/list' });
    adList.value = Array.isArray(res) ? res : [];
  } catch (e) {
    adList.value = [];
  }
};

const logout = () => {
  uni.removeStorageSync('token');
  uni.removeStorageSync('userInfo');
  uni.reLaunch({ url: '/pages/index/index' });
};
</script>

<style scoped>
.admin-container { min-height: 100vh; background-color: #111114; padding: 26rpx; color: #fff; }
.header { margin-bottom: 20rpx; }
.title { font-size: 44rpx; font-weight: bold; color: #00d26a; display: block; }
.subtitle { font-size: 24rpx; color: #858b98; }
.ops-row { display: flex; gap: 16rpx; margin: 16rpx 0; flex-wrap: wrap; }
.btn { background: #2a2f3a; color: #d6d9e0; border-radius: 10rpx; font-size: 24rpx; }
.btn-primary { background: #00d26a; color: #fff; }
.btn-warn { background: #4d3030; color: #ffcaca; }
.panel { margin-top: 20rpx; background: #1a1e28; border: 1px solid #2c3342; border-radius: 16rpx; padding: 20rpx; }
.panel-title { font-size: 30rpx; color: #f0f4ff; font-weight: 600; display: block; margin-bottom: 10rpx; }
.tip { font-size: 22rpx; color: #8790a5; display: block; margin-bottom: 12rpx; }
.form-grid { display: grid; gap: 12rpx; }
.input { background: #242a36; height: 78rpx; border-radius: 10rpx; color: #fff; padding: 0 20rpx; font-size: 24rpx; }
.ph { color: #5f687d; }
.source-list { margin-top: 10rpx; }
.source-item { display: flex; justify-content: space-between; gap: 12rpx; background: #202532; border-radius: 12rpx; padding: 14rpx; margin-bottom: 12rpx; }
.left { flex: 1; min-width: 0; }
.name { color: #fff; font-size: 26rpx; display: block; }
.url { color: #99a1b3; font-size: 22rpx; display: block; margin-top: 6rpx; word-break: break-all; }
.right { display: flex; flex-direction: column; gap: 8rpx; align-items: stretch; }
.status { font-size: 22rpx; color: #d59f7a; text-align: right; }
.status.active { color: #00d26a; }
.small { font-size: 22rpx; background: #2e3544; color: #d7dbea; border-radius: 8rpx; }
.small.danger { background: #4a2a2a; color: #ffb7b7; }
.ad-item { font-size: 24rpx; color: #d7dbea; padding: 8rpx 0; border-bottom: 1px solid #2c3342; }
</style>
