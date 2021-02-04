package com.wzqcode.utils;

import java.util.*;

//图像情感相关标签
public class LabelsInstance {
    private static LabelsInstance labelsInstance;

    private List<String> relaxedLabelList;
    private List<String> sadLabelList;
    private List<String> fearLabelList;

    private LabelsInstance(){
        String[] relaxed = new String[]{"轻松", "愉悦", "安静", "清新", "治愈", "舒适", "娴静","放松"};
        String [] sad = new String[]{"忧郁", "伤感", "感动","忧愁", "孤独", "思念", "悲伤", "萧瑟", "怀念"};
        String[] fear = new String[]{"阴暗", "恐惧", "痛苦", "不安", "绝望"};

         relaxedLabelList = Arrays.asList(relaxed);
        sadLabelList = Arrays.asList(sad);
        fearLabelList = Arrays.asList(fear);
    }

    public static synchronized LabelsInstance getLabelsInstance(){
        if(labelsInstance==null){
            labelsInstance = new LabelsInstance();
            return labelsInstance;
        }
        return labelsInstance;
    }

    //根据情感标签获取展示标签
    public List<String> getLabelResult(String label){
        List<String> result = null;
        switch (label){
            case "1":
                result = randomSelect(3, relaxedLabelList);
                break;
            case "2":
                result = randomSelect(3,sadLabelList);
                break;
            case "3":
                result = randomSelect(3, fearLabelList);
            default:
                break;
        }
        return result;
    }

    /**
     * 从List中随机抽取指定个数的元素
     * @param list 元素列表
     * @param count 抽取数量
     * @return  抽取结果集合
     */
    private List<String> randomSelect(int count , List<String> list){
        if(list.isEmpty() || list.size()<count){
            return null;
        }
        //保存抽取元素索引的集合
        Set<Integer> resultIndex = new HashSet<Integer>();
        List<String> resultList = new ArrayList<>();

        while(resultIndex.size() < count){
            Random random = new Random();
            int i = random.nextInt(list.size());
            resultIndex.add(i);
        }

        //根据索引将随机的元素添加进列表
        for(Integer j: resultIndex){
            resultList.add(list.get(j));
        }

        return resultList;
    }

}
