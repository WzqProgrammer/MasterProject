function publicPath() {
  if (process.env.NODE_ENV == "production") {
    return "./";
  } else {
    return "/";
  }
}

module.exports = {
  publicPath: publicPath(),

  devServer: {
    host: "0.0.0.0", //指定使用一个 host。默认是 localhost，这里默认值即可
    port: 80, //指定端口
    hot: true, // 开启热更新
    https: false, // 是否开启https模式
    proxy: {
      "/api": {
        target: "http://159.75.89.42:8081/",
        changeOrigin: true,
        secure: false,
        pathRewrite: {
          "^/api": "",
        },
      },
    },
  },
};
