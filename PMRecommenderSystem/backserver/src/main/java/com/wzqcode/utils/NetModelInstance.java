package com.wzqcode.utils;

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
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

//对神经网络模型的单例
public class NetModelInstance {

    private static NetModelInstance netModelInstance;

    private static ZooModel model;
    private static Predictor predictor;
    private static String imagePath;

    public String GetImagePath(){
        return imagePath;
    }
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
                .optSynsetArtifactName("synset.txt")
                .setPipeline(pipeline)
                .optApplySoftmax(true)
                .build();
        //加载模型
//        System.setProperty("ai.djl.repository.zoo.location", "build/pytorch_models/mobileNet_v2");

//        String modelPath = ResourceUtils.getURL("classpath:").getPath()
//                +"pytorch_models/" +"mobileNet_v2/"+"mobileNet_v2.pt";
        ClassPathResource resourceModel = new ClassPathResource("pytorch_models/mobileNet_v2/mobileNet_v2.pt");
        ClassPathResource resourceSynset = new ClassPathResource("pytorch_models/mobileNet_v2/synset.txt");

        //模型从jar包中复制出来
        InputStream inputModel = resourceModel.getInputStream();
        InputStream inputSynset = resourceSynset.getInputStream();
        File newModelFile = new File("./models/mobileNet_v2.pt");
        File newSynsetFile = new File("./models/synset.txt");

        FileUtils.copyInputStreamToFile(inputModel, newModelFile);
        FileUtils.copyInputStreamToFile(inputSynset, newSynsetFile);

        Criteria<Image, Classifications> criteria = Criteria.builder()
                .setTypes(Image.class, Classifications.class)
                .optTranslator(translator)
                .optModelUrls(newModelFile.getParentFile().toURI().toURL().toString())
                .optModelName("mobileNet_v2.pt")
                .optProgress(new ProgressBar()).build();
        //模型载入
        System.out.println(criteria.getModelName());
        System.out.println(criteria.getModelZoo());
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
//        String staticPath = ClassUtils.getDefaultClassLoader().getResource("static").getPath();

        File imageDir = new File("./images");
        // 图片存储目录及图片名称
        String url_path = classLabel + File.separator + newFileName;
        //图片保存路径
        String savePath = imageDir.getAbsolutePath() + File.separator + url_path;
        //当前图片的保存路径可被外界访问
        imagePath = savePath;
        System.out.println("图片保存地址："+savePath);

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
