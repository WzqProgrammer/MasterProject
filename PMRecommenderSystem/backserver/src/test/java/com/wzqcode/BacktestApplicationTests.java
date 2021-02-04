package com.wzqcode;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.CenterCrop;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.DownloadUtils;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import com.wzqcode.entity.UserEntity;
import com.wzqcode.service.UserService;
import com.wzqcode.utils.NetModelInstance;
import lombok.var;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
class BacktestApplicationTests {
    @Autowired
    private UserService userService;

    @Test
    void mongoSaveTest(){
        UserEntity userEntity = new UserEntity();
        userEntity.setUserName("wzq34554");
        userEntity.setPassword("1234");
        userService.saveEntity(userEntity);
    }

    @Test
    void contextLoads() throws IOException {
        //模型下载
//        DownloadUtils.download("https://djl-ai.s3.amazonaws.com/mlrepo/model/cv/image_classification/ai/djl/pytorch/resnet/0.0.1/traced_resnet18.pt.gz", "build/pytorch_models/resnet18/resnet18.pt", new ProgressBar());
        DownloadUtils.download("https://djl-ai.s3.amazonaws.com/mlrepo/model/cv/image_classification/ai/djl/pytorch/synset.txt", "build/pytorch_models/resnet18/synset.txt", new ProgressBar());
    }

    @Test
    void deepModelTest() throws MalformedModelException, ModelNotFoundException, IOException, TranslateException {
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
//        System.setProperty("ai.djl.repository.zoo.location", "build/pytorch_models/resnet18");
        System.setProperty("ai.djl.repository.zoo.location", "build/pytorch_models/mobileNet_v2");

        Criteria<Image, Classifications> criteria = Criteria.builder()
                .setTypes(Image.class, Classifications.class)
                .optArtifactId("ai.djl.localmodelzoo:mobileNet_v2")
                .optTranslator(translator)
                .optProgress(new ProgressBar()).build();

        //模型载入
        ZooModel model = ModelZoo.loadModel(criteria);

        var img = ImageFactory.getInstance()
                .fromFile( Paths.get("D:\\CodeProjects\\MasterProject\\PMRecommenderSystem\\backserver\\src\\main\\resources\\static\\images\\dog.png"));
        Predictor predictor = model.newPredictor();
        Classifications classifications = (Classifications) predictor.predict(img);
        System.out.println(classifications.best());
        System.out.println(classifications.best().getClassName());
        System.out.println(classifications.best().getClass());
        System.out.println(classifications.best().getProbability());
    }

    @Test
    void djlLoadFileModelTest() throws MalformedModelException, ModelNotFoundException, IOException, TranslateException {
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
//        System.setProperty("ai.djl.repository.zoo.location", "build/pytorch_models/resnet18");
//        System.setProperty("ai.djl.repository.zoo.location", "build/pytorch_models/mobileNet_v2");

        Criteria<Image, Classifications> criteria = Criteria.builder()
                .setTypes(Image.class, Classifications.class)
                .optTranslator(translator)
                .optModelUrls("build/pytorch_models/mobileNet_v2")
                .optModelName("mobileNet_v2")
                .optProgress(new ProgressBar()).build();

        //模型载入
        ZooModel model = ModelZoo.loadModel(criteria);

        var img = ImageFactory.getInstance()
                .fromFile( Paths.get("D:\\CodeProjects\\MasterProject\\PMRecommenderSystem\\backserver\\src\\main\\resources\\static\\images\\dog.png"));
        Predictor predictor = model.newPredictor();
        Classifications classifications = (Classifications) predictor.predict(img);
        System.out.println(classifications.best());
        System.out.println(classifications.best().getClassName());
        System.out.println(classifications.best().getClass());
        System.out.println(classifications.best().getProbability());
    }
    @Test
    //图片获取测试哦
    void getImageTest() throws IOException {
        var img = ImageFactory.getInstance()
                .fromFile(Paths.get("F:\\JavaProjects\\Vue_SpringBootTest\\backtest\\src\\main\\resources\\static\\images\\dog.png"));
        //获取打包后文件的路径
        URL url = new ClassPathResource("static/images/dog.png").getURL();
        System.out.println(url);
    }

    @Test
    void testNetModelInstance() throws IOException, TranslateException, ModelNotFoundException, MalformedModelException {
        Image img = ImageFactory.getInstance()
                .fromFile( Paths.get("F:\\JavaProjects\\Vue_SpringBootTest\\backtest\\src\\main\\resources\\static\\images\\fear_1.png"));
        String result = NetModelInstance.getInstance().getClassification(img);
        System.out.println(result);
    }

    @Test
    //路径测试
    void urlTest(){
        //获取项目classes/static的地址
        String staticPath = ClassUtils.getDefaultClassLoader().getResource("static").getPath();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String date = df.format(new Date().getTime());// new Date()为获取当前系统时间，也可使用当前时间戳
        System.out.println(date);
    }
}
