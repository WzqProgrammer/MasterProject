import json
import random
import re
import time
from multiprocessing.dummy import Pool

import numpy as np
import requests as rq
from bs4 import BeautifulSoup
from lxml import etree
from tqdm import tqdm, trange


def get_valid_ips(file_path):
    # 从文件中读取可用ip列表
    with open(file_path, 'r', encoding='utf-8') as f:
        valid_ip_list = json.load(f)
    return valid_ip_list


class IPPool():
    """
    针对https://www.kuaidaili.com/free/inha/中代理ip的爬取与构建
    """

    def __init__(self, valid_ip_file, test_ip_url='https://www.ip.cn/', page_count=30):
        """
        Args:
            valid_ip_file (str): 可用ip的路径
            test_ip_url (str): 测试ip可用性的网址
        """
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:70.0) Gecko/20100101 Firefox/70.0'
        }
        self.page_count = page_count
        self.valid_ip_file = valid_ip_file
        self.test_ip_url = test_ip_url
        # 爬取的所有ip，也是需要测试的ip
        self.test_ip_list = []
        # 最终可用ip列表
        self.valid_ip_list = []
        # 当前可用的ip列表
        self.current_vaild_ip_list = []


    def valid_orgin_ip_file(self):
        test_ip_list = self.get_valid_ips_from_file(self.valid_ip_file)
        # 测试当前的ip列表
        self.process_ip_test(test_ip_list, is_to_file=False)
        # 最终可用列表清空
        self.valid_ip_list = []
        

    def get_ip(self, index, ip):
        """获取公开的代理ip

        Args:
            index ([int]): 网页的页码
            ip ([str]): 使用的请求ip，从以往的ip池中获取

        Returns:
            [list]: 该页的ip列表
        """
        ip_list = []
        # 路径
        url = 'https://www.kuaidaili.com/free/inha/' + str(index)
        # 构建代理ip格式
        proxies = {}
        if ip.split(":")[0] == 'http':
            proxies = {
                'http': ip
            }
        else:
            proxies = {
                'https': ip
            }
        # 请求
        response = rq.get(url=url, headers=self.headers, proxies=proxies)
        # 设置编码
        response.encoding = response.apparent_encoding
        response = response.text

        response = etree.HTML(response)

        tr_list = response.xpath('//*[@id="list"]/table/tbody/tr')
        for i in tr_list:
            ip = i.xpath('./td[1]/text()')[0]
            port = i.xpath('./td[2]/text()')[0]
            # 协议
            agreement = str(i.xpath('./td[4]/text()')[0])
            agreement = agreement.lower()
            # 拼装完整ip
            ip = agreement + '://' + ip + ':' + port
            # 将处理好的ip添加到列表中
            ip_list.append(ip)
        return ip_list


    def get_all_ip(self):
        """获取所有ip

        Args:
            page_count (int): 需爬取的网页页数

        """
        # 进度条显示
        with trange(self.page_count) as t:
            for index in range(1, self.page_count+1):
                
                if len(self.current_vaild_ip_list) <= 0:
                    self.current_vaild_ip_list = self.get_valid_ips_from_file(self.valid_ip_file)
                # 随机选择ip
                ip = random.choice(self.current_vaild_ip_list)
                ip_list = self.get_ip(index, ip)
                self.test_ip_list.extend(ip_list)
                # 随机等待时间
                random_time = random.random()
                time.sleep(random_time*2)
                
                t.set_description('ip count: %i' % len(self.test_ip_list))
                t.update(1)
        print("共爬取ip数量为：" + str(len(self.test_ip_list)))
        return self.test_ip_list


    def get_valid_ips_from_file(self, file_path):
        # 从文件中读取可用ip列表
        with open(file_path, 'r', encoding='utf-8') as f:
            current_ip_list = json.load(f)
        return current_ip_list


    def ip_test(self, ip):
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
            response = rq.get(url=self.test_ip_url,
                              headers=self.headers, proxies=proxies, timeout=3)
            self.valid_ip_list.append(ip)
        except Exception as e:
            print(e)



    def process_ip_test(self, test_ip_list, is_to_file=True):
        # 使用多进程测试
        pool = Pool(4)
        pool.map(self.ip_test, test_ip_list)
        print("总共爬取%s个ip，可用ip为：%s，不可用ip为：%s"
              % (len(test_ip_list), len(self.valid_ip_list),len(test_ip_list)-len(self.valid_ip_list)))
        
        self.current_vaild_ip_list = self.valid_ip_list
        
        if is_to_file:
            # 将可用ip写入到文件中
            with open(self.valid_ip_file, 'w', encoding='utf-8') as fw:
                json.dump(self.valid_ip_list, fw)
            
            print('可用ip文件写入完毕')
        else:
            print("当前ip列表测试完毕")
        
        
    def build_ip_pool(self):
        print(self.test_ip_url)
        # 对原有的ip进行可用性测试，得到当前可用的ip列表
        self.valid_orgin_ip_file()
        self.get_all_ip()
        # 完整处理流程
        self.process_ip_test(self.test_ip_list)
        
