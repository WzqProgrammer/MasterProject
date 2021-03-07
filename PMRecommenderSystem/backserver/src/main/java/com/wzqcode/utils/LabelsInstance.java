package com.wzqcode.utils;

import com.wzqcode.entity.TagSongEntity;
import com.wzqcode.service.TagSongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

//图像情感相关标签
@Component
public class LabelsInstance {
    private static LabelsInstance labelsInstance;
    private HashMap<String, List<String>> tagSongIdMap = new HashMap<>();
    private List<String> finalSongIds;

    private List<String> relaxedLabelList;
    private List<String> sadLabelList;
    private List<String> fearLabelList;

    private static List<String> resultEmotions;

    @Autowired
    private TagSongService tagSongService;

    private LabelsInstance(){

        String[] relaxed = new String[]{"安静", "清新", "欢快", "治愈", "温柔","放松","宁静"};
        String [] sad = new String[]{"致郁", "伤感", "感动","丧", "孤独", "悲伤", "唯美", "温柔"};
        String[] fear = new String[]{"诡异","黑暗", "惊悚"};

        relaxedLabelList = Arrays.asList(relaxed);
        sadLabelList = Arrays.asList(sad);
        fearLabelList = Arrays.asList(fear);
//        initTagSongIdMap();
    }

    public static synchronized LabelsInstance getLabelsInstance(){
//        if(labelsInstance==null){
//            labelsInstance = new LabelsInstance();
//            return labelsInstance;
//        }
        return labelsInstance;
    }
    @PostConstruct
    public void init(){
        initTagSongIdMap();
        labelsInstance = this;
        labelsInstance.tagSongService = this.tagSongService;
    }

    /**
     * 初始化标签与其歌曲id的字典
     */
    void initTagSongIdMap(){
        for(String tag: relaxedLabelList){
            System.out.println(tag);
            selectTagSong(tag);
        }
        for(String tag: sadLabelList){
            selectTagSong(tag);
        }
        for(String tag: fearLabelList){
            selectTagSong(tag);
        }
    }

    void selectTagSong(String tagName){

        TagSongEntity tagSongEntity = tagSongService.findSongIdsByTag(tagName);

        List<String> sids = new ArrayList<>();
        for (int i=0;i < tagSongEntity.getRecs().size(); i++) {
            String sid = tagSongEntity.getRecs().get(i).get("sid").toString();
            sids.add(sid);
        };
        tagSongIdMap.put(tagName, sids);
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
        resultEmotions = result;
        return resultEmotions;
    }


    //用户可选择的标签
    public List<String> getSelectedTags() {
        //从三种情感中分别抽取标签
        List<String> resultTags = new ArrayList<>();
        resultTags.addAll(Objects.requireNonNull(randomSelect(2, relaxedLabelList)));
        resultTags.addAll(Objects.requireNonNull(randomSelect(2, sadLabelList)));
        resultTags.addAll(Objects.requireNonNull(randomSelect(1, fearLabelList)));

        return resultTags;
    }

    /**
     * 推荐相关歌曲id
     * @return
     */
    public List<String> recommendSongs(){
        if(tagSongIdMap.isEmpty()){
            initTagSongIdMap();
        }
        finalSongIds = new ArrayList<>();
        for(String tag: resultEmotions){
            List<String> tempIds = randomSelect(4, tagSongIdMap.get(tag));
            System.out.println(tag);
            System.out.println(tempIds);
            finalSongIds.addAll(tempIds);
        }

        return finalSongIds;
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
