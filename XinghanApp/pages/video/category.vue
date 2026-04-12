<template>
  <view class="category-container">
    <global-nav activeMenu="分类" />

    <view class="filter-panel">
      <view class="filter-row">
        <text class="filter-label">分类</text>
        <view class="filter-items">
          <text class="f-item" :class="{ active: query.categoryId === item.id }" v-for="item in categoryFilters" :key="item.id" @click="updateFilter('categoryId', item.id)">{{ item.name }}</text>
        </view>
      </view>

      <view class="filter-row">
        <text class="filter-label">类型</text>
        <view class="filter-items">
          <text class="f-item" :class="{ active: query.type === item }" v-for="item in filters.types" :key="item" @click="updateFilter('type', item)">{{ item }}</text>
        </view>
      </view>

      <view class="filter-row">
        <text class="filter-label">地区</text>
        <view class="filter-items">
          <text class="f-item" :class="{ active: query.region === item }" v-for="item in filters.regions" :key="item" @click="updateFilter('region', item)">{{ item }}</text>
        </view>
      </view>

      <view class="filter-row">
        <text class="filter-label">年份</text>
        <view class="filter-items">
          <text class="f-item" :class="{ active: query.year === item }" v-for="item in filters.years" :key="item" @click="updateFilter('year', item)">{{ item }}</text>
        </view>
      </view>

      <view class="filter-row">
        <text class="filter-label">排序</text>
        <view class="filter-items">
          <text class="f-item" :class="{ active: query.sort === '综合' }" @click="updateFilter('sort', '综合')">综合</text>
          <text class="f-item" :class="{ active: query.sort === '最新' }" @click="updateFilter('sort', '最新')">最新</text>
          <text class="f-item" :class="{ active: query.sort === '最热' }" @click="updateFilter('sort', '最热')">最热</text>
          <text class="f-item" :class="{ active: query.sort === '评分' }" @click="updateFilter('sort', '评分')">评分</text>
        </view>
      </view>
    </view>

    <view class="video-grid">
      <view class="video-card" v-for="video in videoList" :key="video.id" @click="goDetail(video.id)">
        <view class="poster-wrap">
          <image class="poster" :src="video.posterUrl || '/static/default-poster.png'" mode="aspectFill"></image>
          <view class="tag-status">{{ video.updateStatus || '更新中' }}</view>
        </view>
        <view class="video-info">
          <text class="video-title">{{ video.title }}</text>
          <text class="video-sub">{{ video.subCategory || video.region || '未知' }}</text>
        </view>
      </view>
    </view>

    <view class="loading-status">
      <text v-if="loading">努力加载中...</text>
      <text v-else-if="noMore && videoList.length > 0">已经到底啦</text>
      <text v-else-if="videoList.length === 0">暂无相关视频</text>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref, watch } from 'vue';
import { onLoad, onReachBottom } from '@dcloudio/uni-app';
import request from '@/utils/request.js';

const CATEGORY_CACHE_KEY = 'cache:category:list:v1';
const QUERY_CACHE_TTL = 60 * 1000;
const queryCache = new Map();

const filters = reactive({
  types: ['全部', '剧情', '喜剧', '动作', '科幻', '爱情', '悬疑', '动画', '惊悚', '武侠'],
  regions: ['全部', '大陆', '日韩', '欧美', '港台', '泰国', '印度', '其他'],
  years: ['全部', '2026', '2025', '2024', '2023', '2022', '2021', '10年代', '更早']
});

const categoryFilters = ref([{ id: null, name: '全部' }]);
const query = reactive({ categoryId: null, type: '全部', region: '全部', year: '全部', sort: '最热', page: 1, size: 18 });

const videoList = ref([]);
const loading = ref(false);
const noMore = ref(false);
let debounceTimer = null;

watch(() => [query.categoryId, query.type, query.region, query.year, query.sort], () => {
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => loadData(true), 220);
});

const updateFilter = (key, value) => { query[key] = value; };
const goDetail = (id) => uni.navigateTo({ url: `/pages/video/detail?id=${id}` });

const loadCategories = async () => {
  try {
    const local = uni.getStorageSync(CATEGORY_CACHE_KEY);
    if (local?.expireAt > Date.now() && Array.isArray(local.data)) {
      categoryFilters.value = [{ id: null, name: '全部' }, ...local.data.map((c) => ({ id: c.id, name: c.name || `分类${c.id}` }))];
      return;
    }
    const res = await request({ url: '/app/category/list' });
    const categories = Array.isArray(res) ? res : [];
    const normalized = categories.map((c) => ({ id: c.id, name: c.name || `分类${c.id}` }));
    categoryFilters.value = [{ id: null, name: '全部' }, ...normalized];
    uni.setStorageSync(CATEGORY_CACHE_KEY, { data: normalized, expireAt: Date.now() + 5 * 60 * 1000 });
  } catch (e) {}
};

const loadData = async (reset = false) => {
  if (loading.value || (noMore.value && !reset)) return;
  if (reset) {
    query.page = 1;
    noMore.value = false;
    videoList.value = [];
  }

  loading.value = true;
  try {
    const cacheKey = JSON.stringify({
      categoryId: query.categoryId, type: query.type, region: query.region, year: query.year, sort: query.sort, page: query.page, size: query.size
    });

    let res;
    const cacheItem = queryCache.get(cacheKey);
    if (cacheItem && cacheItem.expireAt > Date.now()) {
      res = cacheItem.data;
    } else {
      res = await request({
        url: '/app/video/list',
        data: {
          categoryId: query.categoryId || undefined,
          type: query.type === '全部' ? '' : query.type,
          region: query.region === '全部' ? '' : query.region,
          year: query.year === '全部' ? '' : query.year,
          sort: query.sort,
          page: query.page,
          size: query.size
        }
      });
      queryCache.set(cacheKey, { data: res, expireAt: Date.now() + QUERY_CACHE_TTL });
    }

    const records = res?.records || [];
    videoList.value = reset ? records : [...videoList.value, ...records];
    if (records.length < query.size) noMore.value = true;
    query.page++;
  } finally {
    loading.value = false;
  }
};

onLoad(async (options) => {
  await loadCategories();
  if (options.type && options.type !== '全部') query.type = options.type;
  loadData(true);
});

onReachBottom(() => loadData(false));
</script>

<style scoped>
.category-container { min-height: 100vh; background: #0b0f19; color: #fff; padding-top: 96rpx; }
.filter-panel { background: #101622; padding: 20rpx 18rpx; border-bottom: 1rpx solid #202a3c; }
.filter-row { display: flex; align-items: flex-start; margin-bottom: 14rpx; }
.filter-row:last-child { margin-bottom: 0; }
.filter-label { width: 70rpx; color: #6f7b94; font-size: 22rpx; margin-top: 6rpx; }
.filter-items { flex: 1; display: flex; flex-wrap: wrap; gap: 10rpx; }
.f-item { background: #1a2232; color: #9eabc4; font-size: 20rpx; padding: 8rpx 14rpx; border-radius: 8rpx; border: 1rpx solid transparent; }
.f-item.active { color: #18d96b; border-color: #18d96b; background: rgba(24, 217, 107, 0.1); }
.video-grid { padding: 16rpx 18rpx 20rpx; display: grid; grid-template-columns: repeat(3, 1fr); gap: 14rpx; }
.video-card { background: #141a27; border: 1rpx solid #212b3d; border-radius: 10rpx; overflow: hidden; }
.poster-wrap { position: relative; }
.poster { width: 100%; height: 190rpx; }
.tag-status { position: absolute; right: 8rpx; top: 8rpx; background: #18d96b; color: #fff; font-size: 18rpx; padding: 2rpx 7rpx; border-radius: 6rpx; }
.video-info { padding: 10rpx; }
.video-title { display: block; color: #e8edf7; font-size: 22rpx; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.video-sub { display: block; margin-top: 6rpx; color: #7e8da8; font-size: 20rpx; }
.loading-status { text-align: center; color: #6f809f; font-size: 22rpx; padding: 16rpx 0 24rpx; }
</style>