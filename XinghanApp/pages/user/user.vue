<template>
  <view class="user-container">
    <view class="header-card">
      <image class="avatar" src="/static/default-avatar.png" mode="aspectFill"></image>
      <view class="user-info">
        <text class="nickname">{{ userInfo.username || '未登录' }}</text>
        <text class="vip-status" v-if="userInfo.vipExpireTime">VIP 有效期至: {{ formatDate(userInfo.vipExpireTime) }}</text>
        <text class="vip-status inactive" v-else>未开通 VIP</text>
      </view>
      <button class="small-login" v-if="!isLogin" @click="goLogin">去登录</button>
    </view>

    <view class="menu-list" v-if="isLogin">
      <view class="menu-item" @click="goHistory">
        <text>观看历史</text>
        <text class="arrow">></text>
      </view>
      <view class="menu-item" @click="handleAction('我的金币')">
        <text>我的金币 (0)</text>
        <text class="arrow">></text>
      </view>
      <view class="menu-item" @click="handleAction('设置')">
        <text>应用设置</text>
        <text class="arrow">></text>
      </view>
    </view>

    <view class="history-box" v-if="isLogin">
      <text class="history-title">最近观看</text>
      <view class="history-item" v-for="item in historyList" :key="item.id">
        <text class="h-title">{{ item.videoTitle || '未知视频' }}</text>
        <text class="h-progress">进度 {{ item.playProgress || 0 }}s</text>
      </view>
      <text class="empty" v-if="historyList.length === 0">暂无历史记录</text>
    </view>

    <button class="logout-btn" @click="handleLogout" v-if="isLogin">退出登录</button>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import request from '@/utils/request.js';

const userInfo = ref({});
const isLogin = ref(false);
const historyList = ref([]);

const loadUser = () => {
  const token = uni.getStorageSync('token');
  const localUser = uni.getStorageSync('userInfo') || {};
  isLogin.value = !!token;
  userInfo.value = localUser;
};

const loadHistory = async () => {
  if (!isLogin.value) return;
  try {
    const res = await request({ url: '/app/history/list', data: { page: 1, size: 5 } });
    historyList.value = res?.records || [];
  } catch (e) {
    historyList.value = [];
  }
};

onShow(async () => {
  loadUser();
  await loadHistory();
});

const formatDate = (dateString) => {
  if (!dateString) return '';
  return dateString.replace('T', ' ').substring(0, 10);
};

const handleAction = (name) => {
  uni.showToast({ title: `${name}模块开发中`, icon: 'none' });
};

const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/login' });
};

const goHistory = () => {
  uni.showToast({ title: '可在此扩展历史列表页', icon: 'none' });
};

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token');
        uni.removeStorageSync('userInfo');
        userInfo.value = {};
        isLogin.value = false;
        historyList.value = [];
      }
    }
  });
};
</script>

<style scoped>
.user-container { min-height: 100vh; background-color: #111114; padding: 30rpx; }
.header-card { display: flex; align-items: center; background: linear-gradient(135deg, #2a2a35, #1c1c23); padding: 40rpx 30rpx; border-radius: 20rpx; margin-bottom: 30rpx; }
.avatar { width: 120rpx; height: 120rpx; border-radius: 50%; margin-right: 30rpx; background-color: #333; }
.user-info { display: flex; flex-direction: column; flex: 1; }
.nickname { color: #fff; font-size: 36rpx; font-weight: bold; margin-bottom: 10rpx; }
.vip-status { color: #fadb14; font-size: 24rpx; }
.vip-status.inactive { color: #888; }
.small-login { background: #00d26a; color: #fff; font-size: 24rpx; border-radius: 30rpx; height: 56rpx; line-height: 56rpx; }
.menu-list { background-color: #1c1c23; border-radius: 16rpx; padding: 0 30rpx; }
.menu-item { display: flex; justify-content: space-between; align-items: center; padding: 36rpx 0; border-bottom: 1px solid #2a2a35; color: #e5e5e5; font-size: 30rpx; }
.menu-item:last-child { border-bottom: none; }
.arrow { color: #666; }
.history-box { margin-top: 24rpx; background: #1c1c23; border-radius: 16rpx; padding: 24rpx; }
.history-title { color: #fff; font-size: 30rpx; font-weight: 600; display: block; margin-bottom: 10rpx; }
.history-item { padding: 16rpx 0; border-bottom: 1px solid #2a2a35; }
.history-item:last-child { border-bottom: none; }
.h-title { color: #ddd; display: block; font-size: 26rpx; }
.h-progress { color: #7d7d88; font-size: 22rpx; margin-top: 6rpx; display: block; }
.empty { color: #777; font-size: 24rpx; }
.logout-btn { margin-top: 40rpx; background-color: #2a2a35; color: #ff4d4f; font-size: 32rpx; border-radius: 16rpx; }
.logout-btn::after { border: none; }
</style>