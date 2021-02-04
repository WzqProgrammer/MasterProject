import Vue from "vue";
import VueRouter from "vue-router";
Vue.use(VueRouter);

// 导入对应的路由
import musicExperience from "./views/musicExperience.vue";
import homepage from "./views/homepage.vue";
import login from "./views/login.vue";
import register from "./views/register.vue"

const router = new VueRouter({
  routes: [
    {
      path: "/",
      redirect: "/homepage",
    },
    {
      path: "/homepage",
      name: "homepage",
      component: homepage,
    },
    {
      // 用户登录
      path: "/login",
      name: "login",
      component: login,
    },
    {
      // 用户注册
      path: "/register",
      name: "register",
      component: register,
    },
    {
      // 音乐推荐功能
      path: "/musicExperience",
      name: "musicExperience",
      component: musicExperience,
    },
  ],
});

//挂载路由导航守卫
router.beforeEach((to, from, next)=>{
  //to 将要访问的路径
  //from 代表从哪个路径跳转而来
  //next 一个函数 表示方向
  //next() 放行   next('/login')  强制跳转
  if(to.path == '/login' || to.path == '/homepage' || to.path == '/register') {
    return next()
  }
  //检测是否存在token
  const tokenStr = localStorage.getItem('token')

  if(!tokenStr){
    return next('/login')
  }
  return next()
})

export default router;
