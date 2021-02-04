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
    <div class="top">
      <el-row :gutter="20">
        <el-col :span="18">
          <div class="grid-content bg-purple">
            <i class="el-icon-share"></i>
          </div>
        </el-col>
        <!-- 首页导航栏 -->
        <el-col :span="6">
          <el-menu class="el-menu-demo"
                   :default-active="activeIndex"
                   mode="horizontal"
                   background-color="#16355c"
                   text-color="#fff"
                   active-text-color="#ffd04b">
            <el-menu-item index="1">
              <router-link to="/homepage">首页</router-link>
            </el-menu-item>
            <el-menu-item index="2">
              <router-link to="/login">产品介绍</router-link>
            </el-menu-item>
            <el-menu-item index="3">
              <router-link to="/musicExperience">体验</router-link>
            </el-menu-item>
          </el-menu>
        </el-col>
      </el-row>
    </div>
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
.el-header,.top {
  padding: 0;
  margin: 0;
  border: 0;
  background-color: #16355c;
  color: #333;
  text-align: center;
  line-height: 60px;
}
.el-row {
  padding: 0;
  margin: 0;
  border: 0;
  line-height: 60px;
}

.el-menu-item {
  width: 120px;
}
</style>
