# coding:utf-8
import json
import random
import re
import time

import jieba
import numpy as np
import pymongo
import requests as rq
from tqdm import tqdm, trange


class TextParser():
    """歌曲所在歌单文本的解析器
    """
    
    def __init__(self):
        pass


    def clean_zh_text(self, text):
        """清洗数据，去除特殊字符

        Returns:
            string: completed text
        """
        # keep English, digital and Chinese
        comp = re.compile('[^A-Z^a-z^0-9^\u4e00-\u9fa5^\s^,]')
        return comp.sub('', text)


    def stopwordslist(self, filepath):
        """read the stopwords list from filepath

        Args:
            filepath (str): the path of stopwords file

        Returns:
            list: stopwords list
        """
        stopwords = [line.strip() for line in open(
            filepath, 'r', encoding='utf-8').readlines()]
        # strip() 方法用于移除字符串头尾指定的字符（默认为空格或换行符）或字符序列。
        # readlines()读取所有行并返回列表
        return stopwords


    def seg_sentence(self, sentence):
        """Segmentation of sentences

        Args:
            sentence (string): [description]

        Returns:
            string: completed segmentation of sentences with split space
        """
        # 分词
        sentence_seged = jieba.cut(sentence.strip())
        stopwords = self.stopwordslist(r"./data/stopwords_cn.txt")  # 这里加载停用词的路径
        outstr = ""
        for word in sentence_seged:
            if word not in stopwords:
                if word != '\t':
                    # 以空格为分隔符返回词的字符串
                    outstr += word
                    outstr += " "
        return outstr
    
    
    def parse_playlist_json_to_dict(self, str_json, playlist_id):
        """parsing playlist json info to dict

        Args:
            str_json (str): the json of playlist info from request
            playlist_id (int): playlist id

        Returns:
            dict: the processed and activity dict 
        """
        # 将json转换为dict
        data_dict = json.loads(str_json)
        # 获取所有的歌曲id
        track_ids = data_dict.get("playlist").get("trackIds")
        song_ids = []
        for track in track_ids:
            song_id = track.get('id')
            song_ids.append(song_id)
        # 歌单的标签列表
        tags_list = data_dict.get("playlist").get("tags")
        # 歌单名称
        name = data_dict.get("playlist").get("name")
        # 歌单订阅数量
        subscribedCount = data_dict.get("playlist").get("subscribedCount")
        # 歌单播放数量
        playCount = data_dict.get("playlist").get("playCount")
        # 将数据封装成dict
        playlist_info_dict = {
            "_id": playlist_id,
            "song_ids":song_ids,
            "tags_list": tags_list,
            "name": name,
            "subscribedCount": subscribedCount,
            "playCount": playCount,
        }
        return playlist_info_dict
    
    
    def parse_playlist_to_tags(self, playlist_str):
        """对歌单文本进行清洗、分词、去重和标签列表化

        Returns:
            [list]: 去重的词列表
        """
        # 对BGM标签的正则处理，替换
        pattern = re.compile(r'bgm|背景音乐', re.I)
        result = re.sub(pattern, 'BGM', playlist_str, 0)
        
        result = self.clean_zh_text(result)
        result = self.seg_sentence(result)
        data_list = list(set(result.split(' ')))
        # 返回清洗、分词、去重列表化的数据
        return data_list
    
    
class MongoDAO():
    
    def __init__(self, mongo_config):
        """
        Args:
            mongo_config ([dict]): {'mongo_url': ***, 'db_name': ***, 'collection_name': ***}
        """
        self.mongo_config = mongo_config
        self.set_mongo_config(self.mongo_config)
         
    
    def set_mongo_config(self, mongo_config):
        """设置mongo的连接，选择数据库和集合

        Args:
            mongo_config ([dict]): {'mongo_url': ***, 'db_name': ***, 'collection_name': ***}
        """
        self.mongo_client = pymongo.MongoClient(mongo_config.get('mongo_url'))
        self.mongo_db = self.mongo_client[mongo_config.get('db_name')]
        self.mongo_collection = self.mongo_db[mongo_config.get('collection_name')]
        
        
    def find(self, query=None):
        """mongodb查询

        Args:
            query (dict): {'key': 'value'} 为null是返回collection中所有文档

        """
        if query==None:
            result = self.mongo_collection.find()
        else:
            result = self.mongo_collection.find(query)
        return result
    
    
    def count_documents(self, query={}):
        """the count of the document meet the query condition

        Args:
            query (dict): {'key': 'value'}

        Returns:
            [int]: count
        """
        count = self.mongo_collection.count_documents(query)
        return count
    
    
    def insert_one(self, document):
        """insert a document to the collection

        Args:
            document (dict): {'key': 'value'}
        """
        try:
            self.mongo_collection.insert_one(document)
        except Exception as e:
            print(e)
            
    
    def update_one(self, query, update_task):
        """update one document in the collection

        Args:
            query (dict): {'key': 'value'}
            update_task (dict): {'$set': {'key': 'value'}}
        """
        try:
            self.mongo_collection.update_one(query, update_task)
        except Exception as e:
            print(e)
