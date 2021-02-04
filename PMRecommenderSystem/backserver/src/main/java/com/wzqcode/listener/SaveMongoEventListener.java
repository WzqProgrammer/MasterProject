package com.wzqcode.listener;

import com.wzqcode.annotation.AutoIncrement;
import com.wzqcode.entity.SequenceId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.lang.reflect.Field;

import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

/**
 * @author 14188
 * @date 2021/2/3 15 :11
 * @description 保存文档监听类 在保存对象时，通过反射方式来为其生成ID
 */
@Component
public class SaveMongoEventListener extends AbstractMongoEventListener<Object> {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        final Object source = event.getSource();
        if (source != null) {
            // 使用反射工具类，实现回调接口的方法，对成员进行操作
            ReflectionUtils.doWithFields(source.getClass(), new FieldCallback() {

                @Override
                public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                    ReflectionUtils.makeAccessible(field); // 使操作的成员可访问

                    // 如果是带有@AutoIncrement注解的，成员调用getter方法返回的类型是Number类型的，返回的类型都是0的(没有赋值，默认为0)
                    if (field.isAnnotationPresent(AutoIncrement.class) && field.get(source) instanceof Number
                            && field.getLong(source) == 0) {
                        String collName = source.getClass().getSimpleName().substring(0, 1).toLowerCase()
                                + source.getClass().getSimpleName().substring(1);

                        // setter方法的调用，使ID成员属性，进行自增
                        field.set(source, getNextId(collName));
                    }
                }
            });
        }
    }

    /*
     * @param collName
     *          集合名称，需查找的序列集合名称
     * @return Long 序列值
     */
    private Long getNextId(String collName){
        Query query = new Query(Criteria.where("collName").is(collName));
        Update update = new Update();
        update.inc("seqId", 1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        SequenceId seq = mongoTemplate.findAndModify(query, update, options, SequenceId.class);
        return seq.getSeqId();
    }
}
