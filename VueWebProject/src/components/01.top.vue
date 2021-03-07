<!--
 * @Author: your name
 * @Date: 2021-01-15 18:37:14
 * @LastEditTime: 2021-02-02 17:11:48
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \VueWebProject\src\components\01.top.vue
-->
<template>
  <el-header>
    <!-- 图标文字显示 -->
    <div>
      <span style="color:white;margin-left:20px">IEMR</span>
    </div>
    <!-- 首页导航栏 -->
    <el-menu router="true"
             mode="horizontal"
             background-color="#16355c"
             text-color="#fff"
             active-text-color="#ffd04b">
      <el-menu-item index="1"
                    route="/homepage">
        <!-- <router-link to="/homepage">首页</router-link> -->
        首页
      </el-menu-item>
      <el-menu-item index="2"
                    route="/login">
        <!-- <router-link to="/login">产品介绍</router-link> -->
        登录
      </el-menu-item>
      <el-menu-item index="3"
                    route="/musicExperience">
        <!-- <router-link to="/musicExperience">体验</router-link> -->
        体验
      </el-menu-item>
    </el-menu>

  </el-header>
</template>

<script>
export default {
  name: 'top',
  data() {
    return {
      activeIndex: '1',
    }
  }
}
</script>


<style scoped>
.el-header {
  display: flex;
  justify-content: space-between;
  padding: 0;
  margin: 0;
  border: 0;
  background-color: #16355c;
  color: #333;
  align-items: center;
  /* position: fixed; */
  width: 100%;
  /* line-height: 60px; */
}

.el-menu {
  height: 100%;
}

.el-menu-item {
  width: 120px;
  display: flex;
  justify-content: center;
}
</style>
