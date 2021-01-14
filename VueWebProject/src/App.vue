<template>
  <div id="app">
    <!-- <top /> -->
    <!-- <index /> -->
    <mymusic />
  </div>
</template>

<script>
import top from './components/01.top.vue';
import index from './components/02.index.vue';
import mymusic from './components/mymusic.vue';

export default {
  name: 'app',
  components: {
    // top,
    // index,
    mymusic
  }
};
</script>

<style >

</style>
