<template>
  <view class="home-container">
    <global-nav activeMenu="首页" />

    <view class="hero-wrap">
      <swiper class="banner-swiper" circular autoplay indicator-dots>
        <swiper-item v-for="item in bannerList" :key="item.id">
          <image class="banner-img" :src="item.posterUrl || '/static/default-poster.png'" mode="aspectFill" @click="goDetail(item.id)" />
        </swiper-item>
      </swiper>
    </view>

    <view class="mini-tabs">
      <view class="tab-item" v-for="nav in quickNavs" :key="nav.id" @click="goCategory(nav.name)">
        <text class="tab-icon">{{ nav.icon }}</text>
        <text class="tab-text">{{ nav.name }}</text>
      </view>
    </view>

    <view class="video-section">
      <view class="section-title">热播榜</view>
      <view class="list-wrapper">
        <view class="video-card" v-for="video in videoList" :key="video.id" @click="goDetail(video.id)">
          <view class="cover-wrap">
            <image class="cover" :src="video.posterUrl || '/static/default-poster.png'" mode="aspectFill" />
            <view class="badge">{{ video.updateStatus || '更新中' }}</view>
          </view>
          <text class="title">{{ video.title }}</text>
          <text class="desc">{{ video.subCategory || video.region || '精彩推荐' }}</text>
        </view>
      </view>
      <view class="load-status">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">已经到底啦</text>
      </view>
    </view>

    <view class="go-top" v-if="showTop" @click="scrollToTop">↑</view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onReachBottom, onPullDownRefresh, onPageScroll } from '@dcloudio/uni-app';
import GlobalNav from '@/components/global-nav/global-nav.vue';
import request from '@/utils/request.js';

const bannerList = ref([]);
const videoList = ref([]);
const quickNavs = ref([
  { id: 1, name: '电影', icon: '🎬' },
  { id: 2, name: '剧集', icon: '📺' },
  { id: 3, name: '动漫', icon: '🧩' },
  { id: 4, name: '综艺', icon: '🎤' }
]);

const pageNum = ref(1);
const pageSize = 12;
const loading = ref(false);
const noMore = ref(false);
const showTop = ref(false);

const fetchHomeData = async (reset = false) => {
  if (loading.value || (noMore.value && !reset)) return;
  if (reset) {
    pageNum.value = 1;
    noMore.value = false;
    videoList.value = [];
  }
  loading.value = true;
  try {
    if (reset) {
      const banners = await request({ url: '/app/video/banner' });
      bannerList.value = banners || [];
    }
    const pageData = await request({
      url: '/app/video/list',
      data: { sort: '最热', page: pageNum.value, size: pageSize }
    });
    const records = pageData?.records || [];
    videoList.value = reset ? records : [...videoList.value, ...records];
    if (records.length < pageSize) noMore.value = true;
    pageNum.value += 1;
  } finally {
    loading.value = false;
    uni.stopPullDownRefresh();
  }
};

onLoad(() => fetchHomeData(true));
onPullDownRefresh(() => fetchHomeData(true));
onReachBottom(() => fetchHomeData(false));

onPageScroll((e) => {
  showTop.value = e.scrollTop > 500;
});

const scrollToTop = () => {
  uni.pageScrollTo({ scrollTop: 0, duration: 300 });
};

const goDetail = (id) => uni.navigateTo({ url: `/pages/video/detail?id=${id}` });
const goCategory = (typeName) => uni.navigateTo({ url: `/pages/video/category?type=${encodeURIComponent(typeName)}` });
</script>

<style scoped>
.home-container { min-height: 100vh; background: #0b0f19; color: #fff; padding-top: 96rpx; }
.hero-wrap { padding: 16rpx 18rpx 0; }
.banner-swiper { height: 380rpx; border-radius: 14rpx; overflow: hidden; }
.banner-img { width: 100%; height: 100%; }

.mini-tabs { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14rpx; padding: 20rpx 18rpx; }
.tab-item { background: #161c29; border: 1rpx solid #222b3b; border-radius: 12rpx; padding: 16rpx 0; display: flex; flex-direction: column; align-items: center; }
.tab-icon { font-size: 30rpx; }
.tab-text { color: #c5cede; font-size: 22rpx; margin-top: 6rpx; }

.video-section { padding: 0 18rpx 24rpx; }
.section-title { font-size: 34rpx; font-weight: 700; margin-bottom: 16rpx; }
.list-wrapper { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14rpx; }
.video-card { background: #141a27; border-radius: 10rpx; overflow: hidden; border: 1rpx solid #20293a; }
.cover-wrap { position: relative; }
.cover { width: 100%; height: 190rpx; }
.badge { position: absolute; right: 8rpx; top: 8rpx; background: #18d96b; color: #fff; font-size: 18rpx; padding: 2rpx 8rpx; border-radius: 6rpx; }
.title { display: block; color: #e9edf5; font-size: 22rpx; padding: 10rpx 10rpx 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.desc { display: block; color: #7f8ca3; font-size: 20rpx; padding: 6rpx 10rpx 10rpx; }
.load-status { text-align: center; color: #71809c; font-size: 22rpx; padding: 20rpx 0; }

.go-top { position: fixed; right: 24rpx; bottom: 120rpx; width: 64rpx; height: 64rpx; background: #18d96b; color: #fff; border-radius: 50%; text-align: center; line-height: 64rpx; font-size: 34rpx; z-index: 1000; box-shadow: 0 6rpx 20rpx rgba(24, 217, 107, 0.35); }
</style>