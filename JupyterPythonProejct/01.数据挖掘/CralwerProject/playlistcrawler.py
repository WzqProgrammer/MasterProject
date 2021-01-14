import json
import random
import re
import time
import os
import numpy as np
import requests as rq
from bs4 import BeautifulSoup
from lxml import etree
from tqdm import tqdm, trange

import sys
sys.path.append("..") 

from parsetools import MongoDAO, TextParser
import ip_pool as ip_pool

class PlaylistCrawler():

    def __init__(self, valid_ips_filepath, mongoDAO):
        # 可用ip列表
        self.valid_ips = ip_pool.get_valid_ips(valid_ips_filepath)
        self.headers = {'User-Agent':
                        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36'}
        self.playlist_tags = self.get_playlist_tags(
            './data/playlist_tags.json')
        self.all_playlist_ids = []
        self.mongoDAO = mongoDAO
        self.text_parser = TextParser()

    def get_playlist_tags(self, filepath):
        """from music url get playlist tags

        Returns:
            list: the tags of playlists
        """
        if os.path.exists(filepath):
            with open(filepath, 'r', encoding='utf-8') as fr:
                playlist_tags = json.load(fr)
            return playlist_tags

        playlist_tags = []
        response = rq.get(
            url='https://autumnfish.cn/playlist/catlist')
        data_dict = json.loads(response.text)
        all_subs = data_dict.get("sub")
        # 一级的分类 {'0': '语种', '1': '风格', '2': '场景', '3': '情感', '4': '主题'}
        categories = data_dict.get("categories")

        for sub in all_subs:
            name = sub.get("name")
            # 所有分类标签添加
            playlist_tags.append(name)

        with open(filepath, 'w', encoding='utf-8') as fw:
            json.dump(playlist_tags, fw, ensure_ascii=False)
        return playlist_tags

    def getHTMLText(self, url, headers, ip):
        try:
            proxies = {}
            if ip.split(":")[0] == 'http':
                proxies = {
                    'http': ip
                }
            else:
                proxies = {
                    'https': ip
                }
            r = rq.get(url, headers=headers, proxies=proxies)
            r.raise_for_status()
            r.encoding = r.apparent_encoding
            return r.text
        except:
            return "网络解析错误"

    def parse_main(self, html):
        """parsing html text

        Args:
            html (string): the html text from request

        Returns:
            list: the list of current html page playlist ids 
        """
        soup = BeautifulSoup(html, 'html.parser')
        c = soup.find_all('li')
        for unit in c:
            try:
                name_url = unit.find('a', {'class': "msk"})  # m这里有URL，名字的信息
                # 正则获取歌单的id
                pattern = re.compile(r'\d+')
                id_str = pattern.findall(name_url['href'])
                # 将解析到的歌单id添加到列表中
                self.all_playlist_ids.append(id_str[0])
            except:
                continue

    def get_playlist_ids(self, cat, depth=38):
        """get playlist ids from current category

        Args:
            cat (str): category in url
            depth (int, optional): pages of current category . Defaults to 38.
        """
        start_url = 'https://music.163.com/discover/playlist/?order=hot&cat='+cat
        for i in range(depth):
            try:
                url = start_url+'&limit=35'+'&offset='+str(35*(i))
                # 随机抽取ip
                ip = random.choice(self.valid_ips)
                html = self.getHTMLText(url, self.headers, ip)
                self.parse_main(html)
                # 随机时间等待
                random_time = random.random()
                time.sleep(random_time * 2)
            except:
                print('失败')
                continue
        print(cat + " 标签爬取完毕")
        print("current counts of ids: " + str(len(self.all_playlist_ids)))
        print("========================")

    def get_all_playlist_ids(self, filepath=None):
        """get all playlist ids from response with tags

        Args:
            filepath (string, optional): the path of saving all playlist ids. Defaults to None.

        Returns:
            list: playlist ids
        """
        for category in self.playlist_tags:
            self.get_playlist_ids(category)

        # id去重
        self.all_playlist_ids = list(set(self.all_playlist_ids))
        # 将所有的歌单id写入到文件中
        if filepath != None:
            with open(filepath, 'w') as fw:
                json.dump(self.all_playlist_ids, fw)

        return self.all_playlist_ids

    def process_playlist_info(self, playlist_id):
        """get playlist json data from request, parsing json to dict and save to mongodb

        Args:
            playlist_id (string): playlist id
        """
        start_url = 'https://autumnfish.cn/playlist/detail?id=' + playlist_id
        try:
            # 随机抽取ip
            ip = random.choice(self.valid_ips)
            proxies = {}
            if ip.split(":")[0] == 'http':
                proxies = {
                    'http': ip
                }
            else:
                proxies = {
                    'https': ip
                }
            # 请求API获得json格式的字符串
            str_json = rq.get(url=start_url, proxies=proxies,
                              headers=self.headers)
            # 解析数据成dict
            playlist_info_dict = self.text_parser.parse_playlist_json_to_dict(
                str_json.text, playlist_id)
            # 将数据放入mongodb中
            self.mongoDAO.insert_one(playlist_info_dict)

        except Exception as e:
            print('Error: ' + str(e))

    def process_all_playlist_ids(self):
        # 进度条显示
        with trange(len(self.all_playlist_ids)) as t:
            for i, playlisy_id in enumerate(self.all_playlist_ids):
                self.process_playlist_info(playlisy_id)
                # 随机时间等待
                if(i % 100 == 0):
                    random_time = random.random()
                    time.sleep(random_time * 2)

                t.set_description('Process %i' % i)  # 描述显示在左边
                t.update(1)
