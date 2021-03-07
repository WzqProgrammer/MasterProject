<!--
 * @Author: your name
 * @Date: 2021-01-15 18:37:14
 * @LastEditTime: 2021-02-02 16:45:51
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Ediit
 * @FilePath: \VueWebProject\src\App.vue
-->
<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<script>
import top from './components/01.top.vue'
import musicExperience from './views/musicExperience.vue';
import homepage from './views/homepage.vue';
import login from './views/login.vue'

export default {
  name: 'app',
  components: {
    top,
    homepage,
  }
};
</script>

<style >

</style>
