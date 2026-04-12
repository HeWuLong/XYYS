<template>
  <view class="play-container">
    <global-nav />

    <view class="video-wrapper">
      <video
        id="myVideo"
        class="video-player"
        :src="playUrl"
        :poster="posterUrl"
        :title="videoTitle"
        :autoplay="true"
        :controls="true"
        :show-progress="true"
        :show-fullscreen-btn="true"
        :enable-progress-gesture="true"
        object-fit="contain"
        @timeupdate="onTimeUpdate"
        @loadedmetadata="onLoadedMeta"
      ></video>
    </view>

    <view class="quick-controls">
      <button class="ctrl" @click="seekBy(-10)">后退10s</button>
      <button class="ctrl" @click="togglePlay">{{ paused ? '播放' : '暂停' }}</button>
      <button class="ctrl" @click="seekBy(10)">快进10s</button>
      <text class="time">{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</text>
    </view>

    <view class="control-panel">
      <text class="playing-title">{{ videoTitle }} - 第 {{ currentEp }} 集</text>

      <view class="section" v-if="isM3u8">
        <text class="m3u8-tag">当前播放格式：M3U8</text>
      </view>

      <view class="section">
        <text class="section-title">选集</text>
        <view class="episode-grid">
          <view class="ep-item" :class="{ active: currentEp === ep.index }" v-for="ep in episodeList" :key="ep.index" @click="changeEpisode(ep.index)">
            {{ ep.name }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { nextTick, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import GlobalNav from '@/components/global-nav/global-nav.vue';
import request from '@/utils/request.js';

const videoId = ref('');
const videoTitle = ref('视频播放');
const posterUrl = ref('/static/default-poster.png');
const playUrl = ref('');
const currentEp = ref(1);
const episodeList = ref([{ index: 1, name: '01' }]);
const isM3u8 = ref(false);

const videoCtx = ref(null);
const paused = ref(false);
const currentTime = ref(0);
const duration = ref(0);

const parseEpisodeItems = (playUrlRaw) => {
  const parts = String(playUrlRaw || '').split('#').map((s) => s.trim()).filter(Boolean);
  if (!parts.length) return [{ index: 1, name: '01', url: '' }];

  return parts.map((item, idx) => {
    const [name, url] = item.split('$');
    return {
      index: idx + 1,
      name: (name || `${idx + 1}`).slice(0, 8),
      url: (url || name || '').trim()
    };
  });
};

const initVideoCtx = () => {
  videoCtx.value = uni.createVideoContext('myVideo');
};

const onTimeUpdate = (e) => {
  currentTime.value = Number(e.detail.currentTime || 0);
  duration.value = Number(e.detail.duration || duration.value || 0);
};

const onLoadedMeta = (e) => {
  duration.value = Number(e.detail.duration || 0);
};

const formatTime = (sec) => {
  const s = Math.max(0, Math.floor(sec || 0));
  const m = Math.floor(s / 60).toString().padStart(2, '0');
  const ss = (s % 60).toString().padStart(2, '0');
  return `${m}:${ss}`;
};

const togglePlay = () => {
  if (!videoCtx.value) return;
  if (paused.value) {
    videoCtx.value.play();
    paused.value = false;
  } else {
    videoCtx.value.pause();
    paused.value = true;
  }
};

const seekBy = (delta) => {
  if (!videoCtx.value) return;
  const target = Math.max(0, Math.min((duration.value || 0), currentTime.value + delta));
  videoCtx.value.seek(target);
  currentTime.value = target;
};

const loadVideoResource = async () => {
  const detail = await request({ url: `/app/video/detail/${videoId.value}` });
  if (!detail) return;

  videoTitle.value = detail.title || '视频播放';
  posterUrl.value = detail.posterUrl || '/static/default-poster.png';
  episodeList.value = parseEpisodeItems(detail.playUrl);

  const hit = episodeList.value.find((e) => e.index === currentEp.value) || episodeList.value[0];
  currentEp.value = hit.index;
  playUrl.value = hit.url;

  const format = (detail.playFormat || '').toLowerCase();
  isM3u8.value = format.includes('m3u8') || (playUrl.value || '').includes('.m3u8');

  if (!playUrl.value) {
    uni.showToast({ title: '暂无可播放地址', icon: 'none' });
  }

  await nextTick();
  initVideoCtx();
  paused.value = false;
  currentTime.value = 0;
};

onLoad(async (options) => {
  videoId.value = options.id || '';
  currentEp.value = Number(options.ep) || 1;

  if (!videoId.value) {
    uni.showToast({ title: '参数缺失', icon: 'none' });
    return;
  }

  try {
    await loadVideoResource();
  } catch (e) {
    console.error('播放页加载失败', e);
  }
});

const changeEpisode = async (ep) => {
  if (currentEp.value === ep) return;
  const target = episodeList.value.find((item) => item.index === ep);
  if (!target) return;
  currentEp.value = ep;
  playUrl.value = target.url;
  await nextTick();
  initVideoCtx();
  paused.value = false;
  currentTime.value = 0;
};
</script>

<style scoped>
.play-container { min-height: 100vh; background-color: #0f131d; padding-top: 100rpx; }
.video-wrapper { width: 100%; height: 460rpx; background-color: #000; }
.video-player { width: 100%; height: 100%; }
.quick-controls { display: flex; align-items: center; gap: 12rpx; padding: 20rpx; background: #151b28; }
.ctrl { background: #212a3a; color: #dce2ef; border-radius: 8rpx; font-size: 22rpx; height: 60rpx; line-height: 60rpx; padding: 0 18rpx; }
.time { color: #8f9bb3; font-size: 22rpx; margin-left: auto; }
.control-panel { padding: 26rpx; color: #fff; }
.playing-title { font-size: 34rpx; font-weight: 600; margin-bottom: 20rpx; display: block; color: #18d96b; }
.section { margin-bottom: 26rpx; }
.section-title { font-size: 28rpx; font-weight: 600; margin-bottom: 14rpx; display: block; color: #cfd7e8; }
.m3u8-tag { color: #18d96b; font-size: 22rpx; background: rgba(24, 217, 107, 0.12); padding: 8rpx 14rpx; border-radius: 8rpx; }
.episode-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12rpx; }
.ep-item { background-color: #1a2332; color: #b4c0d7; text-align: center; padding: 16rpx 0; font-size: 22rpx; border-radius: 8rpx; }
.ep-item.active { background-color: #18d96b; color: #fff; font-weight: bold; }
</style>