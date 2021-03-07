<!--
 * @Author: your name
 * @Date: 2021-02-01 11:56:27
 * @LastEditTime: 2021-02-04 09:00:25
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \VueWebProject\src\views\login.vue
-->
<template>
  <div class="login_container">
    <div class="login_box">
      <!-- 头像区域 -->
      <div class="avatar_box">
        <img src="../assets/img/favicon.png">
      </div>
      <!-- 登录表单区域 -->
      <el-form class="login_form"
                ref="loginFormRef"
               :model="loginForm"
               :rules="loginFormRules"
               label-width="80px">
        <!-- 用户名 -->
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="loginForm.userName"></el-input>
        </el-form-item>
        <!-- 密码区 -->
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="loginForm.password"></el-input>
        </el-form-item>
        <!-- 按钮区域 -->
        <el-form-item class="btns">
          <a style="margin-right:20px" href="javascript:void(0)" @click="register">用户注册</a>
          <el-button type="primary" @click="login">登录</el-button>
          <el-button type="info" @click="loginFormReset">重置</el-button>
        </el-form-item>

      </el-form>
    </div>
  </div>

</template>

<script>
export default {
  name: 'login',
  data() {
    return {
      //登录表单的数据绑定对象
      loginForm: {
        userName: '',
        password: ''
      },
      //登录表单验证规则对象
      loginFormRules:{
         userName:[
           {required:true, message: '请输入用户名', trigger: 'blur' },
           {min:3, max:10, message: '长度在 3 到 10 个字符', trigger: 'blur'}
         ],
          password:[
           {required:true, message: '请输入密码', trigger: 'blur' },
           {min:5, max:15, message: '长度在 5 到 15 个字符', trigger: 'blur'}
         ]
         
      }
    }
  },
  methods:{
    //登录表单重置
    loginFormReset(){
      // console.log(this)
      this.$refs.loginFormRef.resetFields()
    },
    
    //用户登录
    login(){
      //登录验证
      this.$refs.loginFormRef.validate(valid => {
        if(!valid) return;
        //规则验证通过则向服务端发起请求
        this.$http.post("/api/user/login", this.loginForm).then((res)=>{
          console.log(res);
          if(res.data.code==0){
            this.$storage.set("token", res.data.token);
            this.$message.success("登录成功")
            //登录成功跳转到主页
            this.$router.push({path:'/homepage'})
          }
          else{
            this.$message.error(res.data.msg);
          }
          
        })
      })
    },

    //用户注册界面跳转
    register(){
      this.$router.push({path:'/register'})
    }
  }
}
</script>

<style lang="less" scoped>
.login_container {
    /* 背景渐变 */
  background: linear-gradient(
    60deg,
    rgb(146, 124, 233) 0%,
    rgb(6, 166, 187) 100%
  );
  height: 100%;
}

.login_box {
  width: 450px;
  height: 300px;
  background-color: #fff;
  border-radius: 3px;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);

  .avatar_box {
    height: 130px;
    width: 130px;
    border: 1px solid #eee;
    border-radius: 50%;
    padding: 10px;
    box-shadow: 0 0 10px #ddd;
    position: absolute;
    left: 50%;
    transform: translate(-50%, -50%);
    background-color: #fff;

    img {
      width: 100%;
      height: 100%;
      border-radius: 50%;
      background-color: #fff;
    }
  }
}

.login_form {
  position: absolute;
  bottom: 0;
  width: 80%;
  margin-left: 30px;
}

.btns {
  display: flex;
  justify-content: flex-end;
}
</style>