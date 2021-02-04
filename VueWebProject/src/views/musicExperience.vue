<template>
  <div>
    <top></top>
    <el-container>
      <el-header>图像音乐推荐</el-header>
      <el-container>
        <el-aside width="400px">
          <el-row>
            <transition name="el-fade-in-linear">
              <el-button v-if="imageUrl"
                         style="margin-top: 10%"
                         type="success"
                         @click="uploadImage">开始推荐</el-button>
            </transition>
          </el-row>

          <el-row>
            <transition name="el-fade-in-linear">
              <el-image class="showImage"
                        v-if="imageUrl"
                        style="width: 240px; height: 240px"
                        :src="imageUrl"
                        :preview-src-list="srcList"></el-image>
            </transition>
          </el-row>

          <el-row>
            <el-upload ref="new_image"
                       class="avatar-uploader"
                       action="#"
                       :show-file-list="false"
                       :on-success="handleAvatarSuccess"
                       :on-change="handleAvatarChange"
                       :on-preview="handlePictureCardPreview"
                       :auto-upload="false">
              <el-button size="small"
                         type="primary">点击上传</el-button>
              <div slot="tip"
                   class="el-upload__tip">只能上传jpg/png文件</div>
            </el-upload>
          </el-row>
        </el-aside>

        <el-container>
          <el-main>
            <el-row :gutter="40"
                    v-show="showSongs"
                    style="height: 560px">
              <!-- 图像分类的标签显示 -->
              <el-col :span="3"
                      style="background-color: #b3c0d1">
                <!-- 标签展示 -->
                <ul>
                  <!-- 过度动画 -->
                  <transition-group name="list"
                                    :duration="2000"
                                    @after-enter="tagsTransitionend">
                    <li v-show="showTags"
                        v-for="(item, index) in tags"
                        :key="index">
                      <el-tag type="success"
                              effect="dark"
                              style="width: 80px">
                        {{ item }}
                      </el-tag>
                    </li>
                  </transition-group>
                </ul>
              </el-col>

              <!-- 推荐歌曲列表显示 -->
              <el-col :span="16"
                      style="background-color: #ccae33">
                <transition :duration="5000">
                  <!-- 歌曲列表显示 -->
                  <table v-show="showSongs"
                         class="el-table"
                         style="line-height: 20px">
                    <thead>
                      <th style="width: 50px">索引</th>
                      <!-- <th></th> -->
                      <th style="width: 200px">音乐标题</th>
                      <th style="width: 180px">歌手</th>
                      <!-- <th>专辑</th> -->
                      <th style="width: 50px">时长</th>
                      <th style="width: 200px">满意度</th>
                    </thead>
                    <tbody>
                      <tr v-for="(item, index) in songs"
                          :key="index"
                          @dblclick="playMusic(item.id)"
                          style="line-height: 20px">
                        <td>{{ index + 1 }}</td>
                        <td>{{ item.name }}</td>
                        <td>{{ item.ar[0].name }}</td>
                        <td>{{ item.dt }}</td>
                        <td>
                          <el-rate v-model="marks[index]"
                                   :allow-half="true"
                                   show-text>
                          </el-rate>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </transition>
              </el-col>

              <!-- 用户自己对图像进行标签选择 -->
              <el-col class="all_selected_tags"
                      :span="5"
                      style="background-color: #d3dce6; height: 540px">
                <h2 v-show="showSongs"
                    style="line-height: 80px">你的感觉</h2>
                <el-row v-show="showSongs"
                        type="flex">
                  <el-col :span="14"
                          v-for="(item, index) in selectTags.slice(0, 2)"
                          :key="index">
                    <div>
                      <el-checkbox v-model="item.isSelected"
                                   :label="item.tag"
                                   border
                                   style="background-color: #2bd5d5">
                      </el-checkbox>
                    </div>
                  </el-col>
                </el-row>

                <el-row v-show="showSongs"
                        type="flex">
                  <el-col :span="14"
                          v-for="(item, index) in selectTags_2_3"
                          :key="index">
                    <div>
                      <el-checkbox v-model="item.isSelected"
                                   :label="item.tag"
                                   border
                                   style="background-color: #2bd5d5">
                      </el-checkbox>
                    </div>
                  </el-col>
                </el-row>

                <el-row v-show="showSongs"
                        type="flex">
                  <el-col :span="14"
                          v-for="(item, index) in selectTags_4_5"
                          :key="index">
                    <div>
                      <el-checkbox v-model="item.isSelected"
                                   :label="item.tag"
                                   border
                                   style="background-color: #2bd5d5">
                      </el-checkbox>
                    </div>
                  </el-col>
                </el-row>

                <el-row v-show="showSongs"
                        type="flex">
                  <el-col :span="14"
                          v-for="(item, index) in selectTags_6_7"
                          :key="index">
                    <div>
                      <el-checkbox v-model="item.isSelected"
                                   :label="item.tag"
                                   border
                                   style="background-color: #2bd5d5">
                      </el-checkbox>
                    </div>
                  </el-col>
                </el-row>

                <el-row v-show="showSongs"
                        type="flex">
                  <el-col :span="14"
                          v-for="(item, index) in selectTags_8_9"
                          :key="index">
                    <div>
                      <el-checkbox v-model="item.isSelected"
                                   :label="item.tag"
                                   border
                                   style="background-color: #2bd5d5">
                      </el-checkbox>
                    </div>
                  </el-col>
                </el-row>

                <el-row v-show="showSongs"
                        type="flex">
                  <el-col :span="14"
                          v-for="(item, index) in selectTags_10_11"
                          :key="index">
                    <div>
                      <el-checkbox v-model="item.isSelected"
                                   :label="item.tag"
                                   border
                                   style="background-color: #2bd5d5">
                      </el-checkbox>
                    </div>
                  </el-col>
                </el-row>
                <el-row v-show="showSongs"
                        type="flex"
                        style="line-height: 60px">
                  <el-button type="primary"
                             round
                             style="text-align: center">确认标签</el-button>
                </el-row>
              </el-col>
            </el-row>
          </el-main>

          <el-footer>
            <!-- 音频播放器 -->
            <div class="player">
              <audio :src="musicUrl"
                     autoplay
                     controls></audio>
            </div>
          </el-footer>
        </el-container>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import axios from 'axios'
import top from "../components/01.top"
export default {
  name: 'musicExperience',
    components:{
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
      marks: [], //用户评分
      musicIDs: [
        434871406,
        22688487,
        475774775,
        492390949,
        1352004027,
        487093203,
        499675131,
        412319416,
        1403918626,
        1448090389,
      ], //音乐id列表
      tags: [],
      showTags: false, //标签显示
      showSongs: false, //歌曲列表显示

      showSelectTags: false,
      allTags: [
        '愉悦',
        '轻松',
        '安静',
        '清新',
        '浪漫',
        '治愈',
        '伤感',
        '孤独',
        '感动',
        '思念',
        '忧郁',
        '兴奋',
        '恐惧',
      ], //当前所有的标签
      selectTags: [], //用户为当前图片选中的标签
      selectTags_0_1: [],
      selectTags_2_3: [],
      selectTags_4_5: [],
      selectTags_6_7: [],
      selectTags_8_9: [],
      selectTags_10_11: [],
    }
  },

  created() {
    this.initTags()
    this.getSongInfoFromID(this.musicIDs)

    axios({
      url: 'https://autumnfish.cn/playlist/detail',
      method: 'get',
      params: {
        id: '812787607',
      },
    }).then((res) => {
      console.log(res)
    })
  },
  methods: {
    uploadImage() {
      let _this = this
      // console.log(_this.$refs.new_image.uploadFiles[0].raw)
      let formData = new FormData()
      formData.append('image_data', this.imageFiles[0])
      axios
        .post('http://localhost:8081/user/imageClassification', formData)
        .then((res) => {
          //对应的展示标签获取
          this.tags = res.data
          //相关标签显示
          this.showTags = true
        })
    },

    //初始化展示标签
    initTags() {
      for (let i = 0; i < this.allTags.length; i++) {
        let tagObj = { isSelected: false, tag: this.allTags[i] }
        this.selectTags.push(tagObj)
      }
      this.selectTags_0_1 = this.selectTags.slice(0, 2)
      this.selectTags_2_3 = this.selectTags.slice(2, 4)
      this.selectTags_4_5 = this.selectTags.slice(4, 6)
      this.selectTags_6_7 = this.selectTags.slice(6, 8)
      this.selectTags_8_9 = this.selectTags.slice(8, 10)
      this.selectTags_10_11 = this.selectTags.slice(10, 12)
    },

    handleAvatarChange(file) {
      this.imageUrl = URL.createObjectURL(file.raw)
      this.imageFiles[0] = file.raw
      this.srcList[0] = this.imageUrl
      console.log('on-change : ' + file.size)
    },

    handleAvatarSuccess(res, file) {
      this.imageUrl = URL.createObjectURL(file.raw)
      console.log(file.url)
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
    },

    tagsTransitionend(el) {
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
        url: 'https://autumnfish.cn/song/detail',
        method: 'get',
        params: {
          ids: str_ids,
        },
      }).then((res) => {
        this.songs = res.data.songs
        this.marks = new Array(this.songs.length)
        //处理时长
        for (let i = 0; i < this.songs.length; i++) {
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
    playMusic(id) {
      axios({
        url: 'https://autumnfish.cn/song/url',
        method: 'get',
        params: {
          id: id,
        },
      }).then((res) => {
        this.musicUrl = res.data.data[0].url
      })
    },
  },
}
</script>

<style>
.all_selected_tags .el-row {
  line-height: 40px;
}
.el-row {
  margin-bottom: 20px;
}
.el-col {
  border-radius: 4px;
}
.el-header,
.el-footer {
  background-color: #b3c0d1;
  color: #333;
  text-align: center;
  line-height: 60px;
}

.el-aside {
  background-color: #d3dce6;
  color: #333;
  text-align: center;
  height: 700px;
}

.el-main {
  background-color: #e9eef3;
  color: #333;
  text-align: center;
  line-height: 160px;
  height: 100%;
}

body > .el-container {
  margin-bottom: 40px;
}

.el-container:nth-child(5) .el-aside,
.el-container:nth-child(6) .el-aside {
  line-height: 260px;
}

.el-container:nth-child(7) .el-aside {
  line-height: 320px;
}

.avatar-uploader {
  margin-top: 20%;
}
.showImage {
  margin-top: 10%;
  width: 200px;
  height: 200px;
}
</style>
