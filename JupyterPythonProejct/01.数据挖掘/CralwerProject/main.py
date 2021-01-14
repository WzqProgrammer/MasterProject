# coding:utf-8
from ip_pool import IPPool
from parsetools import MongoDAO
from playlistcrawler import PlaylistCrawler
import time

if __name__ == "__main__":
    # ip_pool = IPPool('./valid_ips.json')
    # # ip_pool.build_ip_pool()
    # test_ip_list = ip_pool.get_all_ip()
    # ip_pool.process_ip_test(test_ip_list)
    
    # test = '消逝的神罚Music 积 跬 步 以 至 千 里 WanYYing 我将玫瑰藏于身后 时刻期盼着与你赴约 Kalinka 蹦迪 精神小伙儿精神依旧在 精选BMG/超嗨蹦迪 ♡、line.quan、 - dinosauR. ·! 【Russia Deep House】bass收录. 电音Ai_尔☛【DJ咕咚中文歌曲】非常的奈斯 大赦天下专用音乐 开始于朋友验证 结束于红色感叹号 封面选的好 绅士不会少 土嗨DJ 蹦迪选手已就位. DJ英文 纯音乐 电音车载音乐〖土嗨之王〗 土嗨/硬曲/网络BGM/300首 很潮很土很嗨很低調还盗版 【节奏控】DJ 蹦迪 撩人 入耳 激情 劲爆 重低音效果的DJ多多 Read All About It (Shoby House Rework) 『摇头｜蹦迪｜车载-仙曲』 我走在长街中，听戏子唱京城 你爹的资料好看吗 DJ FAKE 『一❤只蹦圣贤迪♪』 EA7-DJ精选歌单 A8L W12 酒吧摇头电音House - 全网最火的DJ 看见花瓣散漫了江面，你我相看缺不曾生厌 精神小伙，大赦天下 又好听却不知道歌名。 好听且得劲99 ♞[车载超重低音DJ] 一个暖男大叔的听歌列表 Dian Dian Dian yin 精选ξ电音易燃分享【持续更新】 欧美复古朋克节奏控 酷到沒朋友~樂 『硬曲 硬不硬听了再说（蹦迪专用❗）』 你绝对搜不到的土嗨神曲 EA7/康纳麦格雷戈 头一扭 曲一嗨 鬼火当成路虎开 法拉域Bass House匪气 嗨爆星空 dj-toxic顶级夜场嗨曲 热门劲爆DJ歌曲 深夜写作业艾薇巴蒂嗨起来(｢･ω･)｢ 【私藏】熟悉却忘记名字的电音 『精选』和别人炫耀过你...但还是丢了 精选～网红热歌 爸爸棒爸爸呗爸爸very 今夜 857 战术蹦迪 北执·ᴍᴜsɪᴄ ғᴍ ʀᴇᴍɪx -. 超嗨舞曲串烧！（苏卡比列） Dancers. 打游戏必备神曲 一生只蹦圣迪蹦 精神Dj全网最嗨，摇就完事了 精神小妹的社会歌 别把我听土嗨的事告诉别人～ 幹飯人.幹飯魂.乾飯都是人上人 长春奥斯卡DR.OSCAR 今天从《Tonight 》听起|私人雷达 你搜不到的神仙爆嗨歌单！！ 「DJ歌曲」最熟悉咚鼓版dj 尔兰超爱听的音乐 今天从《努力工作吧》听起/潮人必备歌单 你携秋水揽星河 我有多么爱你. 热门BGM . 你还在找的BGM吗? 这才是真正的深夜网易云！ 2020年最火精神小伙dj GC、犄角嘴真甜 咚鼓版中文DJ 所以爱会消失不见的对不对？ 中文版咚鼓版歌曲+电音♡[不定时更]媄玉儿 鼓咚中文DJ版 中国土嗨【一个人的土嗨夜晚】 时代在洗牌而你永不淘汰 土到極致 便是潮 让我们听点土嗨吧！ 百听不腻的（网络流行） 网络热门BGM〖持续更新〗 君上陪玩厅主持控场/蹦迪bgm 王者农药€超燃节奏 非洲小白的土嗨歌单 皮皮虾精选BGM 流行歌曲 电竞 L&Z Music 抑着浓浓的匪气重低音 来一首精神鸦片，精神一下午 皮皮搞笑专属BGM 『车载』EA7/抖腿/炸街/电驾/慎入 离开我你会遇见更好的 『驾车必备』DJ中文歌曲~嗨起来 土嗨神曲\xa0♪\xa0车载抖腿~提神醒脑 车载流行歌曲集 车载超重低音 dj舞曲/蹦迪 热搜 『中文·土嗨·精选』！ 我是你永远得不到的爸爸™ 车载咚鼓DJ夜店蹦迪电音慢摇 Εа７ø车载音乐⚡️ 超级好听的DJ（电音） 摇头抖腿曲 车载必备ぃ上头带点土嗨 2020开车必备嗨曲 精选BGM｜蹦迪吗超嗨【有点上头】持更 精选| 超燃BGM！ 精选蹦迪炸街硬核神曲(戴上耳机效果更佳) 一心只蹦圣贤迪™❤️ Rye皮皮搞笑虾-段子出品 听最豪华的歌 开最贵的车 神仙般的欧美风. 歌单名正在上传中…… 喜欢骑摩托在路上的男孩. 【全网精选】车载中文dj土嗨合集 【DJ咕咚中文歌曲】非常的奈斯 全网最火的音乐都在这《纯音乐》 A cup of milk tea, take off happily. Ｍｒ＿LH0312 一天不抖，浑身难受 【精选高配】点进来听听吧 也许你喜欢呢 凤舞九天音乐工厂 超重低音 DJ 开车和健身专用 中文dj土嗨至上No.1摇头甩屁屁 聆听顶级车载电音，感受极致听觉盛宴㈡ 道路千万条，安全第一条 汽车DJ摇滚流行那种很飘的下班旅途中！ 【另类咚鼓】虞姬/七妹/糖糖/承利/兮雅 〖精选歌曲DJ〗｛XAX DJ } 中文DJ·咚鼓版·弹鼓版·烟嗓版（Remix） 来蹦迪不？全是女的！ 渣到深处自然爱 咕咚dj.蹦迪歌曲.非常nice giao酱（莹酱）直播歌单 语音直播/主持/拍卖/带飞/蹦迪/节奏/BGM MVP-娱乐传媒 生僻字（吃鸡版） 跳自闭城的大佬专属BGM DJ dj我想飛 哪吒 苏六 扎哥 张诗尧 混的人 精神小伙/精神小妹令— 排排整起 【伤感｜杂乱】你麻木了吗？ “祝你快乐 不止圣诞这一天.” 【禁】～dj-toxic顶级夜场嗨曲 故事都讲烂了 却还是意难平 跳自闭城的大佬专属BGM 、 者思范专辑Remix丶 影视剪辑BGM 美发沙龙发廊音乐节奏 荣耀 我是你无法跨越的高峰 电耗子专属BGM. DJ纯音乐网络热歌 【车载中文dj】开车必备，激情四射！ 蹦迪 嗨曲 Dj中文合集 炒鸡好听的咕咚版歌曲—————— DJ DJ 我想要 飞 正能量/大赦天下/土嗨/精 神 水 龙 弹 张诗尧 哪吒 混的人苏六 李牌牌 鹤轩 扎哥 那些你搜不到的土嗨神曲【蹦迪专用】 热播电音旋律【精选】持续更新ღ℡ （重机车专用）（车载）骑士日常必备 EA7水龙弹之术 温柔歌单“持续更新” 喜欢抒情？dj？快节奏？古风？这里都有！ 用户480170549的歌单 对抗路必备，你就是第四座塔 『超燃BGM』游戏专用 广交朋友，少碰爱情，多蹦迪 中文DJ/咚鼓版/弹鼓版（Remix） 中文咚鼓弹鼓版背景音乐[ DJ阿鑫]慢更. [中文咚鼓/DJ]嗨起来 男人贵在稳重c 经典慢摇DJ串烧歌曲， 0202文艺复兴时代精神领袖 一个人的DJ（DJ and 咚鼓弹鼓版） 「『潮音』混搭种子选手」 先擦鼻涕后提裤从此走上社会路 『偏愛亦是救赎』 苏6 哪吒 小条 防 扎 尧 英雄会 抖腿． 『偏愛亦是救赎』 【DJ土嗨歌曲】熟悉的小抒情 温柔电音，车载音乐，BGM 『偏愛亦是救赎』 精神小伙专用BGM(甩头） 『BGM』你一定听过的神级的背景音乐 你搜不到的土嗨歌曲 ғᴍ ʀᴇᴍɪx -. 社会人 精神小伙 蹦迪摇头必备。 【旋律控】超带感的英文歌 节奏控=带感=dj土嗨=燃向=踩点 『雾起爱意散』 今日份快乐已送达 劲爆 <DJ中文歌曲> 很火 优质DJ气质摇 渣渣辉☞车载☜老哥带DJ 中心藏之 ，何日忘之！ 「晚 風 吹 起 愛 意」 『硬曲』哈哈哈哈哈哈哈 自律的人有多可怕 最新歌曲Dj 车载音乐 土到极致便是潮·热播/DJ 「中文咚鼓」热播·上头曲 「中文咚鼓」热播·上头弹鼓曲 2020【车载 酒吧 DJ 嗨曲 抖腿】 2020蹦迪系- 『谁不想要一套丝滑小连招呢』 华语；XDD；稳健二字挂心头 ԅ(¯ㅂ¯ԅ) 好听的英文歌曲，送给最美的你！'
    # result = playlist_parse.parse_playlist_to_tags(test)
    # print(result)
    
    mongo_config = {
        'mongo_url': 'mongodb://192.168.206.100:27017/',
        'db_name': 'springTest',
        'collection_name': 'test_play_list_2'
    }
    mongoDAO = MongoDAO(mongo_config)
    
    # result =  mongoDAO.find({'_id': 2})
    # for item in result:
    #     print(item)
    
    # start_time = time.time()
    # count = mongoDAO.count_documents({'_id': 1})
    # end_time = time.time()
    # print(str(count) + '   ' + 'time spent : ' + str(end_time - start_time))
    
    # new_book = {'_id': 4, 'bookName': '水杯的', 'bookCounts': 198, 'detail': '好看', '_class': 'com.wzqcode.pojo.Book'}
    # mongoDAO.insert_one(new_book)

    # mongoDAO.update_one({'_id': 1}, {'$delete': {'bookname': 'C++入门到静态'}})
    
    # start_time = time.time()
    # result =  mongoDAO.count_documents({'_id': 858356832})
    # end_time = time.time()
    # print(str(result) + '   ' + 'time spent : ' + str(end_time - start_time))
    
    playlist_crawler = PlaylistCrawler('./data/valid_ips.json', mongoDAO)
    
    tags = playlist_crawler.get_playlist_tags('./data/playlist_tags.json')
    
    print(tags)
