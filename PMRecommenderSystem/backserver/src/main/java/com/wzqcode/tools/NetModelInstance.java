package com.wzqcode.tools;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.CenterCrop;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import org.springframework.util.ClassUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

//对神经网络模型的单例
public class NetModelInstance {

    private static NetModelInstance netModelInstance;

    private static ZooModel model;
    private static Predictor predictor;

    private NetModelInstance(){}

    public static synchronized NetModelInstance getInstance(){
        if(netModelInstance == null){
            netModelInstance = new NetModelInstance();
            return netModelInstance;
        }
        return netModelInstance;
    }

    //初始化模型分类器
    private void initClassification() throws MalformedModelException, ModelNotFoundException, IOException {
        //创建一个Translator
        Pipeline pipeline = new Pipeline();
        pipeline.add(new Resize(256))
                .add(new CenterCrop(244, 244))
                .add(new ToTensor())
                .add(new Normalize(
                        new float[]{0.485f, 0.456f, 0.406f},
                        new float[]{0.229f, 0.224f, 0.225f}
                ));
        Translator<Image, Classifications> translator = ImageClassificationTranslator.builder()
                .setPipeline(pipeline)
                .optApplySoftmax(true)
                .build();
        //加载模型
        System.setProperty("ai.djl.repository.zoo.location", "build/pytorch_models/mobileNet_v2");

        Criteria<Image, Classifications> criteria = Criteria.builder()
                .setTypes(Image.class, Classifications.class)
                .optArtifactId("ai.djl.localmodelzoo:mobileNet_v2")
                .optTranslator(translator)
                .optProgress(new ProgressBar()).build();
        //模型载入
        model = ModelZoo.loadModel(criteria);
        //预测器
        predictor = model.newPredictor();
    }

    //根据图片进行预测
    public String getClassification(Image img) throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
        if(predictor == null){
            initClassification();
        }
        Classifications classifications = (Classifications) predictor.predict(img);
        return classifications.best().getClassName().split(" ")[0];
    }

    /**
     * 保存用户上传的文件到相应的路径
     * @param classLabel  该图片所在的分类标签
     * @param newFileName 新的文件名称
     * @param file  文件类型
     */
    public Boolean saveImage(String classLabel,String newFileName, MultipartFile file){
        if(file.isEmpty()){
            return false;
        }
        //获取项目classes/static的地址
        String staticPath = ClassUtils.getDefaultClassLoader().getResource("static").getPath();

        // 图片存储目录及图片名称
        String url_path = "images"+File.separator + classLabel + File.separator + newFileName;
        //图片保存路径
        String savePath = staticPath + File.separator + url_path;
        System.out.println("图片保存地址："+savePath);
        // 访问路径=静态资源路径+文件目录路径
        String visitPath ="static/" + url_path;
        System.out.println("图片访问uri："+visitPath);

        File saveFile = new File(savePath);
        if (!saveFile.exists()){
            saveFile.mkdirs();
        }
        try {
            file.transferTo(saveFile);  //将临时存储的文件移动到真实存储路径下
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
