<!--
 * @Author: your name
 * @Date: 2021-02-01 11:54:27
 * @LastEditTime: 2021-02-18 10:34:22
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \VueWebProject\src\views\homepage.vue
-->
<template>
  <div>
    <el-container>
      <top></top>
      <div id="BG">
        <!-- 标题区域 -->
        <div class="lnner-header">
          <h1>基于图片情感的音乐推荐</h1>
        </div>
        <span></span>
        <!-- 波浪区域 -->
        <div>
          <!-- svg 形状 -->
          <svg class="waves"
               viewBox="0 24 150 28"
               preserveAspectRatio="none"
               fill="rgba(255,255,255,0.7)"
               shape-rendering="auto">
            <!-- 形状容器 -->
            <defs>
              <path id="gentle-wave"
                    d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z" />
            </defs>
            <!-- 组合形状 -->
            <g class="parallax">
              <use xlink:href="#gentle-wave"
                   x="48"
                   y="0"
                   fill="rgba(255,255,255,0.7)" />
              <use xlink:href="#gentle-wave"
                   x="48"
                   y="3"
                   fill="rgba(255,255,255,0.5)" />
              <use xlink:href="#gentle-wave"
                   x="48"
                   y="5"
                   fill="rgba(255,255,255,0.3)" />
              <use xlink:href="#gentle-wave"
                   x="48"
                   y="7"
                   fill="#fff" />
            </g>
          </svg>
        </div>
      </div>

      <el-main class="introduce">
        <div class="introduce-title">
          <h2>科技与灵感 音乐与图像</h2>
          <p>艺术不是技艺，它是艺术家体验了的感情的传达，艺术与科学既不同而又互相关联；它们在审美的方面交会</p>
        </div>
        <div class="introduce-icons">
          <img src="../assets/img/relaxed-1.png">
          <img src="../assets/img/relaxed-2.png">
          <img src="../assets/img/sad-1.png">
          <img src="../assets/img/sad-2.png">
          <img src="../assets/img/fear-1.png">
          <img src="../assets/img/fear-2.png">
        </div>
        <div class="introduce-text"></div>
        <p>以上的图片均来自游戏内截图，游戏画面与音乐最直接的目的便是引发玩家情感的共鸣，从而提高游玩时的沉浸感</p>
        <p>悲伤、轻松、恐惧、感动......</p>
        <p>本系统当前可根据上传的图片识别出其基本情感，并且基于该情感推荐相关音乐，希望您可以为推荐结果做出评价，谢谢！</p>
      </el-main>
    </el-container>
  </div>

</template>

<script>
import top from "../components/01.top"

export default {
  name: 'homepage',
  components: {
    top
  }
}
</script>

<style lang="less" scoped>
.el-container {
  display: flex;
  flex-direction: column;
}

.introduce {
  display: flex;
  flex-direction: column;
  background-color: #16355c;
  color: #333;
  text-align: center;

  .introduce-icons{
    display: flex;
    justify-content: space-between;
    height: 140px;
    margin-top: 40px;

    >img{
      border-radius: 4px;
      width:140px;
      height:100%
    }
  }

  .introduce-text{
    margin-top: 40px;
  }
}

.el-menu {
  background-color: #16355c;
}

/* 波浪效果 */
h1 {
  font-family: 'Lato', 'sans-serif';
  font-weight: 300;
  /* font-spacing:2px; */
  font-size: 48px;
}

#BG {
  display: flex;
  flex-direction: column;
  height: 100%;
  margin: 0;
  padding: 0;
  position: relative;
  text-align: center;
  /* 背景渐变 */
  background: linear-gradient(
    60deg,
    rgb(146, 124, 233) 0%,
    rgb(6, 166, 187) 100%
  );
  color: wheat;
}
.el-main {
  height: 600px;
  position: relative;


  background-color: rgb(255, 255, 255);

}
.lnner-header {
  height: 540px;
  width: 100%;
  margin: 0;
  padding: 0;
  /* flex */
  /* 垂直居中 */
  display: flex;
  justify-content: center;
  align-items: center;
}
.waves {
  position: relative;
  width: 100%;
  height: 300px;
  margin-bottom: -7px;
  /* 最小值 */
  min-height: 100px;
  /* 最大值 */
  max-height: 150px;
}
.parallax > use {
  animation: move-forever 25s cubic-bezier(0.55, 0.5, 0.45, 0.5) infinite;
}
/* 选择的一个use */
.parallax > use:nth-child(1) {
  /* 延迟 2s 执行 */
  animation-delay: -2s;
  /* 7s 内 执行完毕 */
  animation-duration: 7s;
}
.parallax > use:nth-child(2) {
  animation-delay: -3s;
  animation-duration: 10s;
}
.parallax > use:nth-child(3) {
  animation-delay: -4s;
  animation-duration: 13s;
}
.parallax > use:nth-child(4) {
  animation-delay: -5s;
  animation-duration: 20s;
}
/* 动画 */
@keyframes move-forever {
  0% {
    transform: translate3d(-90px, 0, 0);
  }
  100% {
    transform: translate3d(85px, 0, 0);
  }
}
/* 当屏幕小于 768px 执行 */
@media (max-width: 768px) {
  .waves {
    height: 30%;
    min-height: 30%;
  }
  h1 {
    font-size: 24px;
  }
}
</style>