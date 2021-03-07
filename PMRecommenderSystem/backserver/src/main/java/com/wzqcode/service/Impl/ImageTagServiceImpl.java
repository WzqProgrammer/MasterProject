package com.wzqcode.service.Impl;

import com.wzqcode.dao.ImageTagDAO;
import com.wzqcode.entity.ImageTagEntity;
import com.wzqcode.service.ImageTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 14188
 * @date 2021/2/10 10 :44
 * @description
 */
@Service
public class ImageTagServiceImpl implements ImageTagService {

    @Autowired
    private ImageTagDAO imageTagDAO;

    @Override
    public void saveImageTagData(ImageTagEntity imageTagEntity) {
        imageTagDAO.saveImageTagData(imageTagEntity);
    }
}
