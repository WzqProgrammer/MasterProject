package com.wzqcode.controller;

import ai.djl.MalformedModelException;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;
import com.wzqcode.pojo.Book;
import com.wzqcode.service.BookService;
import com.wzqcode.tools.LabelsInstance;
import com.wzqcode.tools.NetModelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    //查询所有书籍信息
    @GetMapping("/findAll")
    public List<Book> findAllBooks(){
        return bookService.queryAllBook();
    }

    @PostMapping("/addBook")
    public String addBook(@RequestBody Book book){
        return bookService.addBook(book);
    }

    @GetMapping("/findBookById/{id}")
    public Book findBookById(@PathVariable("id") Integer id){
        return bookService.queryBookById(id);
    }

    @PutMapping("/updateBook")
    public long updateBook(@RequestBody Book book){
        return bookService.updateBook(book);
    }

    @DeleteMapping("/deleteBook/{id}")
    public long deleteBook(@PathVariable("id") Integer id){
        return bookService.deleteBookById(id);
    }

    @PostMapping("/imageClassification")
    public List<String> imageClassification(@RequestParam(name = "image_data", required = false) MultipartFile file)
            throws IOException, TranslateException, ModelNotFoundException, MalformedModelException {
        if(file.isEmpty()){
            return null;
        }
        Image img = ImageFactory.getInstance().fromInputStream(file.getInputStream());
        String result = NetModelInstance.getInstance().getClassification(img);

        //使用当前时间戳为文件命名
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");//设置日期格式
        String currentTime = df.format(new Date().getTime());// new Date()为获取当前系统时间，也可使用当前时间戳
        String imageName = currentTime + ".jpg";
        //保存图片文件
        NetModelInstance.getInstance().saveImage(result, imageName,file);
        //将随机抽取的标签元素返回
        return LabelsInstance.getLabelsInstance().getLabelResult(result);
    }
}
