<template>
  <el-container direction="vertical">

    <top></top>

    <el-container class="main_div">
      <el-aside class="aside_div">
        <div class="btn_start_recommender">
          <transition name="el-fade-in-linear">
            <el-button v-if="imageUrl"
                       type="success"
                       @click="uploadImage">开始推荐</el-button>
          </transition>
        </div>

        <div class="image_show">
          <transition name="el-fade-in-linear">
            <el-image class="showImage"
                      v-if="imageUrl"
                      style="width: 240px; height: 240px"
                      :src="imageUrl"
                      :preview-src-list="srcList"></el-image>
          </transition>
        </div>

        <div class="image_upload">
          <el-upload ref="new_image"
                     action="#"
                     :show-file-list="false"
                     :on-success="handleAvatarSuccess"
                     :on-change="handleAvatarChange"
                     :on-preview="handlePictureCardPreview"
                     :auto-upload="false">
            <el-button size="small"
                       type="primary">点击上传</el-button>
            <div slot="tip"
                 class="el-upload__tip">jpg/png文件，对于非2D画面推荐会存在误差</div>
          </el-upload>
        </div>
      </el-aside>

      <el-container>
        <el-main>
          <!-- 图像分类的标签显示 -->
          <div class="image_tags_show">
            <!-- 标签展示 -->
            <ul class="ul_tags"
                v-show="showTags">
              <li v-for="(item, index) in tags"
                  :key="index">
                <el-tag type="success"
                        effect="dark"
                        style="width: 80px">
                  {{ item }}
                </el-tag>
              </li>
            </ul>
          </div>

          <!-- 推荐歌曲列表显示 -->
          <div class="song_list_show"
               style="height:100%"
               v-show="showSongs">

            <el-table :data="songs"
                      @row-dblclick="playMusic"
                      stripe
                      style="width: 100%;height:100%">
              <el-table-column type="index"
                               label="索引"
                               width="60">
              </el-table-column>
              <el-table-column prop="name"
                               label="音乐标题"
                               width="280">
              </el-table-column>
              <el-table-column prop="ar[0].name"
                               label="歌手">
              </el-table-column>
              <el-table-column prop="dt"
                               label="时长">
              </el-table-column>
              <el-table-column label="满意度"
                               width="200">
                <template slot-scope="scope">
                  <!-- 用户评分 -->
                  <el-rate v-model="scope.row.mark"
                           @change="rateChange(scope.row.id, scope.row.mark)"
                           :allow-half="true"
                           show-text>
                  </el-rate>
                </template>
              </el-table-column>
            </el-table>

          </div>

          <!-- 用户自己对图像进行标签选择 -->
          <div class="all_selected_tags"
               style="background-color: #d3dce6">
            <h2 style="line-height: 80px">你的感觉</h2>

            <div class="user_tags"
                 v-if="showSelectTags"
                 @change="checkedTagChanged">
              <el-checkbox-group v-model="selectTags">
                <el-checkbox ref="tag_checkbox"
                             v-for="item in allTags"
                             :label="item"
                             :key="item"
                             border
                             style="background-color: #2bd5d5">
                  {{item}}
                </el-checkbox>
              </el-checkbox-group>
            </div>

            <div class="confirm_btn">
              <el-button type="primary"
                         @click="confirm_tags"
                         round
                         style="text-align: center">确认标签</el-button>
            </div>
          </div>

        </el-main>

        <el-footer>
          <!-- 音频播放器 -->
          <div class="player">
            <audio class="audio_player"
                   :src="musicUrl"
                   autoplay
                   controls></audio>
          </div>
        </el-footer>
      </el-container>
    </el-container>
  </el-container>

</template>

<script>
import axios from 'axios'
import top from "../components/01.top"
export default {
  name: 'musicExperience',
  components: {
    top
  },
  data() {
    return {
      imageUrl: '', //图片
      file: '',
      imageFiles: [], //图片文件列表
      srcList: [], //预览图片列表
      songs: [], //歌曲信息列表
      musicUrl: '', //音乐播放链接
      markInfo: {
        songId: '',
        mark: ''
      },
      musicIDs: [], //音乐id列表
      tags: [],
      showTags: true, //标签显示
      showSongs: true, //歌曲列表显示
      showSelectTags: false,
      allTags: [], //当前所有的标签
      selectTags: [], //用户为当前图片选中的标签
    }
  },

  created() {

  },
  methods: {
    uploadImage() {
      let _this = this
      // console.log(_this.$refs.new_image.uploadFiles[0].raw)
      let formData = new FormData()
      formData.append('image_data', this.imageFiles[0])
      axios
        .post('/api/songRecommend/imageClassification', formData)
        .then((res) => {
          console.log(res)
          //对应的展示标签获取
          this.showSelectTags = true
          this.tags = res.data.tags
          this.musicIDs = res.data.songs
          this.allTags = res.data.selectedTags
          this.selectTags = []
          this.getSongInfoFromID(this.musicIDs)
          //相关标签显示     
          this.showSelectTags = true
        })
    },

    //初用户选择相应标签
    selected_tags(item) {
      if (this.selectTags.indexOf(item) != -1) {
        this.selectTags = this.selectTags.filter((x) => { return x != item })
        return
      }
      this.selectTags.push(item)
    },

    checkedTagChanged(val) {
      console.log(this.selectTags)
    },

    //用户提交自己选择的标签
    confirm_tags() {
      this.$http({
        url: "/api/songRecommend/imageTagSave",
        data: { "tagList": this.selectTags },
        headers: { "token": this.$storage.get("token") },
        method: "POST"
      }).then((res) => {
        console.log(res)
      })

    },

    //评分改变事件
    rateChange(song_id, mark) {
      this.markInfo.songId = song_id
      this.markInfo.mark = mark

      //将该评分与歌曲id发送给后端
      this.$http({
        url: "/api/mark/userSongMark",
        data: this.markInfo,
        headers: { "token": this.$storage.get("token") },
        method: "POST"
      }).then((res) => {
        console.log(res)
      })
    },

    handleAvatarChange(file) {
      this.imageUrl = URL.createObjectURL(file.raw)
      this.imageFiles[0] = file.raw
      this.srcList[0] = this.imageUrl
    },

    handleAvatarSuccess(res, file) {
      this.imageUrl = URL.createObjectURL(file.raw)
    },
    handleRemove(file) {
      console.log(file)
    },

    handlePictureCardPreview(file) {
      this.dialogImageUrl = this.imageUrl
      this.dialogVisible = true
    },

    handleDownload(file) {
      console.log(file)
    },

    //开始推荐
    startRecommend() {
      //相关标签显示
      this.showTags = true
      this.showSongs = true

    },

    //根据歌曲id获取歌曲信息
    getSongInfoFromID(ids) {
      //id列表处理
      let str_ids = ''
      for (let i = 0; i < ids.length; i++) {
        str_ids += ids[i]
        if (i != ids.length - 1) {
          str_ids += ','
        }
      }
      axios({
        url: 'http://cloud-music.pl-fe.cn/song/detail',
        method: 'get',
        params: {
          ids: str_ids,
        },

      }).then((res) => {

        this.songs = res.data.songs
        //处理时长
        for (let i = 0; i < this.songs.length; i++) {
          this.songs[i].mark = 0;

          let duration = this.songs[i].dt
          let min = parseInt(duration / 1000 / 60)
          if (min < 10) {
            min = '0' + min
          }
          let sec = parseInt((duration / 1000) % 60)
          if (sec < 10) {
            sec = '0' + sec
          }
          this.songs[i].dt = `${min}:${sec}`
        }
      })
    },

    //播放音乐
    playMusic(row, event) {
      axios({
        url: 'http://cloud-music.pl-fe.cn/song/url',
        method: 'get',
        params: {
          id: row.id,
        },
      }).then((res) => {
        this.musicUrl = res.data.data[0].url
      })
    },
  },
}
</script>

<style lang="less" scoped>
.el-container {
  height: 100%;
}

.main_div {
  // background-image: url("../assets/img/bg_1.jpg");
  padding: 0;
  height: 100%;
  width: 100%;
  position: relative;
}

.aside_div {
  background-color: #d3dce6;
  color: #333;
  display: flex;
  flex-direction: column;
  text-align: center;
  height: 100%;
  width: 20%;
  align-items: center;
  position: relative;

  .btn_start_recommender {
    position: absolute;
    top: 15%;
  }

  .image_show {
    position: absolute;
    top: 30%;
  }

  .image_upload {
    position: absolute;
    top: 80%;
  }
}

.el-footer {
  background-color: #b3c0d1;
  color: #333;
  text-align: center;
  line-height: 60px;
  padding: 0;

  .player {
    height: 100%;
    width: 100%;
    bottom: 0;
    left: 0;

    .audio_player {
      width: 80%;
    }
  }
}

.el-main {
  padding: 0;
  background-color: #e9eef3;
  color: #333;
  text-align: center;
  height: 100%;
  display: flex;
  padding: 0;

  .image_tags_show {
    display: flex;
    flex-direction: column;

    padding: 0;
    margin: 0;
    width: 12%;
    height: 100%;

    .ul_tags {
      margin-top: 150px;
      height: 100%;
      padding: 0;
    }
    .ul_tags > li {
      padding: 20px;
    }
  }

  .song_list_show {
    width: 70%;
    height: 100%;
  }

  .all_selected_tags {
    display: flex;
    flex-direction: column;
    width: 18%;
    height: 100%;
    align-content: center;
    position: relative;

    .user_tags {
      width: 100%;
      align-content: center;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 0;
      .el-checkbox {
        margin: 0;
        width: 60%;
        margin-top: 20px;
      }
    }

    .confirm_btn {
      width: 100%;
      position: absolute;
      top: 75%;
    }
  }
}
</style>
