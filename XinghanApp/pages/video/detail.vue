<template>
  <view class="detail-container" v-if="video">
    <global-nav :isTransparent="true" />

    <view class="backdrop-wrapper">
      <image class="backdrop-img" :src="video.posterUrl || '/static/default-poster.png'" mode="aspectFill"></image>
      <view class="backdrop-mask"></view>
    </view>

    <view class="info-section">
      <text class="title">{{ video.title || '加载中...' }}</text>
      <view class="tags-row">
        <text class="tag year">{{ video.publishYear || '未知' }}</text>
        <text class="tag type">{{ video.subCategory || '视频' }}</text>
        <text class="tag-text">{{ video.region || '未知地区' }}</text>
        <text class="tag-text">共 {{ episodeList.length }} 集</text>
      </view>

      <view class="meta-info">
        <text class="meta-line truncate-2">简介: {{ ext.description || '暂无简介' }}</text>
      </view>

      <view class="action-row">
        <view class="play-btn" @click="handlePlay(currentEp)">
          <text class="icon">▶</text>
          <text>立即播放</text>
        </view>
      </view>
    </view>

    <view class="player-section">
      <scroll-view scroll-x class="source-scroll" :show-scrollbar="false">
        <view class="source-list">
          <view class="source-item" :class="{ active: currentSource === index }" v-for="(source, index) in sources" :key="index" @click="currentSource = index">
            <text class="source-name">@ {{ source.name }}</text>
          </view>
        </view>
      </scroll-view>

      <view class="episode-grid">
        <view class="ep-item" :class="{ active: currentEp === ep.index }" v-for="ep in episodeList" :key="ep.index" @click="selectEpisode(ep.index)">
          {{ ep.name }}
        </view>
      </view>
    </view>

    <view class="recommend-section">
      <view class="section-title">猜你喜欢</view>
      <view class="recommend-grid">
        <view class="rec-card" v-for="rec in recommends" :key="rec.id" @click="goDetail(rec.id)">
          <image class="rec-poster" :src="rec.posterUrl || '/static/default-poster.png'" mode="aspectFill"></image>
          <text class="rec-title">{{ rec.title }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import GlobalNav from '@/components/global-nav/global-nav.vue';
import request from '@/utils/request.js';

const video = ref(null);
const ext = ref({});
const sources = ref([{ name: '默认源' }]);
const episodeList = ref([{ index: 1, name: '01' }]);
const currentSource = ref(0);
const currentEp = ref(1);
const recommends = ref([]);

onLoad(async (options) => {
  if (!options.id) return;
  await fetchVideoDetail(options.id);
  await fetchRecommend(options.id, video.value?.categoryId);
});

const parseEpisodes = (playUrlRaw, fallbackCount) => {
  if (!playUrlRaw) {
    const count = Number(fallbackCount) || 1;
    return Array.from({ length: count }, (_, i) => ({ index: i + 1, name: (i + 1).toString().padStart(2, '0') }));
  }

  const parts = String(playUrlRaw)
    .split('#')
    .map((s) => s.trim())
    .filter(Boolean);

  if (!parts.length) {
    return [{ index: 1, name: '01' }];
  }

  return parts.map((item, idx) => {
    const [name] = item.split('$');
    return {
      index: idx + 1,
      name: (name || `${idx + 1}`).slice(0, 8)
    };
  });
};

const fetchVideoDetail = async (id) => {
  try {
    const res = await request({ url: `/app/video/detail/${id}`, method: 'GET' });
    if (!res) return;
    video.value = res;

    let extInfo = {};
    try {
      extInfo = res.extInfo ? JSON.parse(res.extInfo) : {};
    } catch (e) {}
    ext.value = extInfo;

    episodeList.value = parseEpisodes(res.playUrl, res.episodeCount);
    currentEp.value = 1;
  } catch (e) {
    console.error('获取详情失败', e);
  }
};

const fetchRecommend = async (currentVideoId, categoryId) => {
  try {
    const data = await request({
      url: '/app/video/recommend',
      data: { currentVideoId, categoryId }
    });
    recommends.value = Array.isArray(data) ? data : [];
  } catch (e) {
    recommends.value = [];
  }
};

const handlePlay = (ep) => {
  const token = uni.getStorageSync('token');
  if (!token) {
    uni.showToast({ title: '请先登录后观看', icon: 'none' });
    setTimeout(() => uni.navigateTo({ url: '/pages/login/login' }), 800);
    return;
  }

  uni.navigateTo({
    url: `/pages/video/play?id=${video.value.id}&ep=${ep}&source=${currentSource.value}`
  });
};

const selectEpisode = (ep) => {
  currentEp.value = ep;
  handlePlay(ep);
};

const goDetail = (id) => {
  uni.redirectTo({ url: `/pages/video/detail?id=${id}` });
};
</script>

<style scoped>
.detail-container { min-height: 100vh; background-color: #111114; color: #fff; padding-bottom: 60rpx; position: relative; overflow-x: hidden; }
.backdrop-wrapper { position: absolute; top: 0; left: 0; width: 100vw; height: 800rpx; z-index: 0; }
.backdrop-img { width: 100%; height: 100%; opacity: 0.5; display: block; }
.backdrop-mask { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(to right, #111114 10%, rgba(17,17,20,0.4) 100%), linear-gradient(to top, #111114 0%, rgba(17,17,20,0) 100%); }
.info-section { position: relative; z-index: 10; padding: 80rpx 40rpx 40rpx; margin-top: 100rpx; }
.title { font-size: 64rpx; font-weight: bold; margin-bottom: 30rpx; display: block; }
.tags-row { display: flex; align-items: center; flex-wrap: wrap; margin-bottom: 30rpx; font-size: 24rpx; }
.tag { padding: 4rpx 12rpx; border-radius: 6rpx; margin-right: 16rpx; font-weight: bold; }
.tag.year { background-color: #f39c12; color: #fff; }
.tag.type { background-color: #00d26a; color: #fff; }
.tag-text { color: #999; margin-right: 20rpx; }
.meta-line { color: #bbb; font-size: 26rpx; line-height: 1.8; display: block; }
.truncate-2 { display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2; overflow: hidden; }
.action-row { display: flex; align-items: center; margin-top: 20rpx; }
.play-btn { background-color: #00d26a; color: #fff; font-size: 30rpx; font-weight: bold; padding: 18rpx 40rpx; border-radius: 12rpx; display: flex; align-items: center; }
.play-btn .icon { margin-right: 10rpx; }
.player-section { position: relative; z-index: 10; padding: 0 40rpx; margin-top: 20rpx; }
.source-scroll { white-space: nowrap; margin-bottom: 30rpx; }
.source-list { display: inline-flex; }
.source-item { margin-right: 30rpx; color: #888; font-size: 26rpx; padding-bottom: 10rpx; }
.source-item.active { color: #00d26a; font-weight: bold; }
.episode-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16rpx; }
.ep-item { background-color: rgba(255,255,255,0.05); color: #bbb; text-align: center; padding: 16rpx 0; font-size: 24rpx; border-radius: 8rpx; }
.ep-item.active { background-color: #00d26a; color: #fff; font-weight: bold; }
.recommend-section { padding: 40rpx; }
.section-title { font-size: 32rpx; font-weight: bold; margin-bottom: 20rpx; }
.recommend-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20rpx; }
.rec-poster { width: 100%; height: 220rpx; border-radius: 10rpx; }
.rec-title { display: block; margin-top: 8rpx; font-size: 24rpx; color: #ddd; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>