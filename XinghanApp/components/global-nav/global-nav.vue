<template>
  <view class="nav-container" :class="{ transparent: isTransparent }">
    <view class="nav-left">
      <view class="logo-box" @click="goHome">
        <text class="logo-icon">🎬</text>
        <text class="logo-text">星瀚影视</text>
      </view>

      <view class="menu-list">
        <text class="menu-item" :class="{ active: activeMenu === '首页' }" @click="goHome">首页</text>
        <text class="menu-item" @click="goCategory('短剧')">短剧</text>
        <text class="menu-item" @click="goCategory('剧集')">剧集</text>
        <text class="menu-item" @click="goCategory('电影')">电影</text>
        <text class="menu-item" @click="goCategory('动漫')">动漫</text>
        <text class="menu-item" @click="goCategory('综艺')">综艺</text>
      </view>
    </view>

    <view class="nav-right">
      <view class="search-box" @click="goCategory('全部')">
        <text class="search-placeholder">搜片名</text>
      </view>

      <view class="icon-btn" @click="openLoginDialog">
        <text class="icon-text">{{ isLogin ? '👤' : '登录' }}</text>
      </view>

      <view class="app-btn">APP</view>
    </view>
  </view>

  <view class="mask" v-if="showLoginDialog" @click="closeLoginDialog">
    <view class="login-dialog" @click.stop>
      <view class="dialog-header">
        <text class="dialog-title">{{ isRegister ? '用户注册' : '用户登录' }}</text>
        <text class="dialog-close" @click="closeLoginDialog">×</text>
      </view>

      <view class="input-group">
        <input class="input-item" v-model="form.username" placeholder="手机/登录账号" placeholder-class="ph-color" />
      </view>
      <view class="input-group">
        <input class="input-item" password v-model="form.password" placeholder="登录密码" placeholder-class="ph-color" />
      </view>
      <view class="input-group" v-if="isRegister">
        <input class="input-item" v-model="form.inviteCode" placeholder="邀请码（选填）" placeholder-class="ph-color" />
      </view>

      <button class="submit-btn" @click="submitLogin">{{ isRegister ? '注册并登录' : '登录账号' }}</button>

      <view class="dialog-footer">
        <text class="link" @click="goForgot">密码找回</text>
        <text class="link" @click="isRegister = !isRegister">{{ isRegister ? '已有账号，去登录' : '没有账号，立即注册' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import request from '@/utils/request.js';

defineProps({
  isTransparent: { type: Boolean, default: false },
  activeMenu: { type: String, default: '' }
});

const isLogin = ref(false);
const showLoginDialog = ref(false);
const isRegister = ref(false);
const clickTimes = ref([]);
const form = reactive({ username: '', password: '', inviteCode: '' });

const checkLogin = () => {
  const token = uni.getStorageSync('token');
  isLogin.value = !!token;
};

onShow(() => checkLogin());

const goHome = () => uni.reLaunch({ url: '/pages/index/index' });
const goCategory = (type) => uni.navigateTo({ url: `/pages/video/category?type=${encodeURIComponent(type)}` });
const goForgot = () => uni.navigateTo({ url: '/pages/login/forgot_password' });

const openLoginDialog = () => {
  if (isLogin.value) {
    uni.switchTab({ url: '/pages/user/user' });
    return;
  }

  const now = Date.now();
  clickTimes.value = [...clickTimes.value.filter((t) => now - t <= 3000), now];
  if (clickTimes.value.length >= 5) {
    clickTimes.value = [];
    uni.navigateTo({ url: '/pages_admin/login/index' });
    return;
  }

  showLoginDialog.value = true;
};

const closeLoginDialog = () => {
  showLoginDialog.value = false;
};

const submitLogin = async () => {
  if (!form.username || !form.password) {
    uni.showToast({ title: '账号和密码不能为空', icon: 'none' });
    return;
  }

  uni.showLoading({ title: '请稍候...', mask: true });
  try {
    if (isRegister.value) {
      await request({ url: '/app/register', method: 'POST', data: form });
    }
    const res = await request({
      url: '/app/login',
      method: 'POST',
      data: { username: form.username, password: form.password }
    });
    uni.hideLoading();
    uni.setStorageSync('token', res.token);
    uni.setStorageSync('userInfo', res);
    isLogin.value = true;
    showLoginDialog.value = false;
    uni.showToast({ title: '登录成功', icon: 'success' });
  } catch (e) {
    uni.hideLoading();
  }
};
</script>

<style scoped>
.nav-container {
  height: 96rpx;
  background: #0d1018;
  border-bottom: 1rpx solid #20242e;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24rpx;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
}
.nav-container.transparent {
  background: linear-gradient(to bottom, rgba(13, 16, 24, 0.94), rgba(13, 16, 24, 0));
}
.nav-left,.nav-right { display: flex; align-items: center; }
.logo-text { font-size: 34rpx; color: #18d96b; font-weight: 700; margin-right: 30rpx; }
.menu-list { display: flex; gap: 22rpx; }
.menu-item { color: #a9b0bf; font-size: 24rpx; }
.menu-item.active { color: #18d96b; }
.search-box { width: 190rpx; height: 52rpx; border-radius: 10rpx; background: #1a1f2b; display: flex; align-items: center; padding: 0 16rpx; margin-right: 14rpx; }
.search-placeholder { color: #667085; font-size: 22rpx; }
.icon-btn { background: #1a1f2b; border-radius: 8rpx; height: 52rpx; padding: 0 16rpx; display: flex; align-items: center; margin-right: 12rpx; }
.icon-text { color: #d4d8e2; font-size: 22rpx; }
.app-btn { background: #18d96b; color: #fff; font-size: 22rpx; border-radius: 8rpx; height: 52rpx; line-height: 52rpx; padding: 0 18rpx; }

.mask { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.55); z-index: 1001; display: flex; align-items: center; justify-content: center; }
.login-dialog { width: 640rpx; background: #10151f; border-radius: 14rpx; padding: 28rpx; border: 1rpx solid #222839; }
.dialog-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18rpx; }
.dialog-title { color: #fff; font-size: 36rpx; font-weight: 600; }
.dialog-close { color: #c6ccda; font-size: 42rpx; }
.input-group { background: #171e2b; border-radius: 10rpx; margin-bottom: 16rpx; }
.input-item { height: 84rpx; color: #fff; padding: 0 20rpx; font-size: 28rpx; }
.ph-color { color: #697186; }
.submit-btn { margin-top: 8rpx; background: #18d96b; color: #fff; border-radius: 10rpx; font-size: 30rpx; }
.submit-btn::after { border: none; }
.dialog-footer { margin-top: 16rpx; display: flex; justify-content: space-between; }
.link { color: #18d96b; font-size: 22rpx; }
</style>