<template>
  <view class="admin-login-container">
    <view class="card">
      <text class="title">管理员登录</text>
      <text class="sub">仅管理员账号可访问后台</text>

      <view class="input-group">
        <input class="input-item" v-model="form.username" placeholder="管理员用户名" placeholder-class="ph" />
      </view>
      <view class="input-group">
        <input class="input-item" type="password" v-model="form.password" placeholder="管理员密码" placeholder-class="ph" />
      </view>

      <button class="submit" @click="handleLogin">进入管理后台</button>
      <button class="back" @click="goHome">返回首页</button>
    </view>
  </view>
</template>

<script setup>
import { reactive } from 'vue';
import request from '@/utils/request.js';

const form = reactive({
  username: '',
  password: ''
});

const handleLogin = async () => {
  if (!form.username || !form.password) {
    uni.showToast({ title: '请输入账号密码', icon: 'none' });
    return;
  }

  uni.showLoading({ title: '登录中', mask: true });
  try {
    const res = await request({
      url: '/app/login',
      method: 'POST',
      data: form
    });
    uni.hideLoading();

    if (!res?.isAdmin) {
      uni.showToast({ title: '当前账号不是管理员', icon: 'none' });
      return;
    }

    uni.setStorageSync('token', res.token);
    uni.setStorageSync('userInfo', res);
    uni.showToast({ title: '管理员登录成功', icon: 'success' });
    setTimeout(() => {
      uni.redirectTo({ url: '/pages_admin/dashboard/index' });
    }, 600);
  } catch (e) {
    uni.hideLoading();
  }
};

const goHome = () => {
  uni.reLaunch({ url: '/pages/index/index' });
};
</script>

<style scoped>
.admin-login-container {
  min-height: 100vh;
  background: #0f1013;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}
.card {
  width: 100%;
  background: #171920;
  border: 1px solid #2a2f3a;
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
}
.title {
  display: block;
  color: #00d26a;
  font-size: 42rpx;
  font-weight: 700;
}
.sub {
  display: block;
  color: #8b92a1;
  font-size: 24rpx;
  margin: 12rpx 0 30rpx;
}
.input-group {
  margin-bottom: 24rpx;
  background: #1f2430;
  border-radius: 12rpx;
}
.input-item {
  height: 88rpx;
  padding: 0 24rpx;
  color: #fff;
  font-size: 28rpx;
}
.ph { color: #596074; }
.submit {
  margin-top: 10rpx;
  background: #00d26a;
  color: #fff;
  border-radius: 12rpx;
}
.back {
  margin-top: 20rpx;
  background: #2a2f3a;
  color: #c6cbd7;
  border-radius: 12rpx;
}
</style>