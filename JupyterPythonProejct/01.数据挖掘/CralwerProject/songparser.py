# coding:utf-8
import json
import random
import re
import time
import os
import jieba
import numpy as np
import pymongo
import requests as rq
from tqdm import tqdm, trange
from parsetools import MongoDAO, TextParser

class SongParser():
    
    def __init__(self, mongoDAO):
        self.mongoDAO = mongoDAO
        self.text_parser = TextParser()
        self.all_songs_dict = {}
        self.all_song_list = []
        self.playlist_tags = self.get_playlist_tags('./data/playlist_tags.json')
        
    def get_playlist_tags(self, filepath):
        if os.path.exists(filepath):
            with open(filepath, 'r', encoding='utf-8') as fr:
                playlist_tags = json.load(fr)
            return playlist_tags
        else:
            return None
        
        
    def song_data_concat(self, all_data):
        """process all playlist data to song data, concat tags and playlist name

        Args:
            all_data (mongodb.collection.find()): the data of playlist from mongodb find()
            
        Returns:
            [list]: the list of song data dict, [{'_id':76483, 'tags':['综合', '电音'], 'play_lists':'心动的信号第三季BG'}, ....]
        """
        with trange(all_data.collection.count()) as t:
            for item in all_data:
                song_ids = item.get("song_ids")
                tags_list = item.get("tags_list")
                name = item.get("name")    # 歌单名称
                for song_id in song_ids:
                    # 构建歌曲dict
                    song_dict = {}
                    song_dict['_id'] = song_id
                    song_dict['tags'] = tags_list
                    song_dict['play_lists'] = name
                    # 该歌曲的id已经存在
                    if song_id in self.all_songs_dict.keys():
                        # 将新的内容加入原数据中 
                        orgin_tags = self.all_songs_dict[song_id].get('tags')
                        orgin_play_lists = self.all_songs_dict[song_id].get('play_lists')
                        # 合并标签列表和歌单名称
                        new_tags = list(set(orgin_tags + tags_list))
                        new_play_lists = name + ' ' + orgin_play_lists
                        # 更新字典数据
                        self.all_songs_dict[song_id]['tags'] = orgin_tags
                        self.all_songs_dict[song_id]['play_lists'] = new_play_lists
                    # 加入歌曲列表
                    else:
                        self.all_songs_dict[song_id] = song_dict
                t.update(1)
        # 将字典转换成列表
        for item in self.all_songs_dict.values():
            self.all_song_list.append(item)
        return self.all_song_list
    
    
    def process_song_data_to_mongo(self, song_data, str_key='_id'):
        """parsing the tags and platlist text in song data, save to mongodb

        Args:
            song_data (dict): {'_id': ***, 'play_list': ***, 'tags': [***]}
            str_key (str, optional): the query condition of mongodb. Defaults to '_id'.
        """
        try:
            # 根据—_id的查询条件
            myquery_id = {'_id':song_data.get(str_key)}
            # 处理当前的歌单文本信息
            data_list = self.text_parser.parse_playlist_to_tags(song_data.get('play_lists'))
            # 判断数据库是否已有该歌曲
            result_count = self.mongoDAO.count_documents(myquery_id)
            if result_count == 0:
                new_song_data = {}
                
                orgin_tags = song_data.get('tags')
                for item in data_list:
                    if item in self.playlist_tags:
                        if item not in orgin_tags:
                            orgin_tags.append(item)
                            
                new_song_data['_id'] = song_data.get(str_key)
                new_song_data['tags'] = orgin_tags
                self.mongoDAO.insert_one(new_song_data)
            else:
                # 对已有的歌曲标签进行更新
                result_data = self.mongoDAO.find(myquery_id)
                orgin_data_dict = result_data[0]
                orgin_tags = orgin_data_dict.get('tags')
                
                for item in data_list:
                    if item in self.playlist_tags:
                        if item not in orgin_tags:
                            orgin_tags.append(item)
                # mongodb修对应的改歌曲tags
                self.mongoDAO.update_one(myquery_id, {'$set': {'tags':orgin_tags}})
        except Exception as e:
            print('save_song_data_mongodb error:' + str(e))
            
    def process_all_song_data(self, all_song_data):
        """process all song data from the list of song data

        Args:
            all_song_data (list): the list of song data dict
        """
        with trange(len(all_song_data)) as t:
            for song_data in all_song_data:
                self.process_song_data_to_mongo(song_data)
                t.update(1)