<!--
 * @Author: your name
 * @Date: 2021-02-01 11:56:27
 * @LastEditTime: 2021-02-04 09:26:44
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \VueWebProject\src\views\register.vue
-->
<template>
  <div class="register_container">
    <div class="register_box">
      <!-- 头像区域 -->
      <div class="avatar_box">
        <img src="../assets/img/favicon.png">
      </div>
      <!-- 登录表单区域 -->
      <el-form class="register_form"
                ref="registerFormRef"
               :model="registerForm"
               :rules="registerFormRules"
               label-width="80px">
        <!-- 用户名 -->
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="registerForm.userName"></el-input>
        </el-form-item>
        <!-- 密码区 -->
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="registerForm.password"></el-input>
        </el-form-item>
          <el-form-item label="重复密码" prop="repassword">
          <el-input type="password" v-model="registerForm.repassword"></el-input>
        </el-form-item>
        <!-- 按钮区域 -->
        <el-form-item class="btns">
          <el-button type="primary" @click="register">注册</el-button>
          <el-button type="info" @click="registerFormReset">重置</el-button>
        </el-form-item>

      </el-form>
    </div>
  </div>

</template>

<script>
export default {
  name: 'register',
  data() {
    var validatePass2 = (rule, value, callback)=>{
        if(value==''){
          callback(new Error('请再次输入密码'))
        }else if(value!=this.registerForm.password){
          callback(new Error('两次输入密码不一致'))
        }else{
          callback()
        }
    }
    return {
      //登录表单的数据绑定对象
      registerForm: {
        userName: '',
        password: '',
        repassword:''
      },
      //登录表单验证规则对象
      registerFormRules:{
         userName:[
           {required:true, message: '请输入用户名', trigger: 'blur' },
           {min:3, max:10, message: '长度在 3 到 10 个字符', trigger: 'blur'}
         ],
          password:[
           {required:true, message: '请输入密码', trigger: 'blur' },
           {min:5, max:15, message: '长度在 5 到 15 个字符', trigger: 'blur'}
         ],
         repassword:[
            {required:true, validator: validatePass2, trigger: 'blur' },
         ]
         
      }
    }
  },
  methods:{
    //登录表单重置
    registerFormReset(){
      // console.log(this)
      this.$refs.registerFormRef.resetFields()
    },
    
    //用户登录
    register(){
      //登录验证
      this.$refs.registerFormRef.validate(valid => {
        if(!valid) return;
        //规则验证通过则向服务端发起请求
        this.$http.post("/user/register", this.registerForm).then((res)=>{
          console.log(res)
          if(res.data.code==0){
            this.$message.success("注册成功");
            this.$router.push({path:'/login'})
          }
          else{
            this.$message.error(res.data.msg);
          }
        })
      })
    }
  }
}
</script>

<style lang="less" scoped>
.register_container {
  background-color: #2b4b6b;
  height: 100%;
}

.register_box {
  width: 450px;
  height: 360px;
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

.register_form {
  position: absolute;
  bottom: 0;
  width: 100%;
  padding: 0 20px;
}

.btns {
  display: flex;
  justify-content: flex-end;
}
</style>