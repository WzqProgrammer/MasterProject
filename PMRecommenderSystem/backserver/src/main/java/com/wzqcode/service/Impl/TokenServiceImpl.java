package com.wzqcode.service.Impl;

import com.wzqcode.dao.TokenDAO;
import com.wzqcode.entity.TokenEntity;
import com.wzqcode.service.TokenService;
import com.wzqcode.utils.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

/**
 * @author 14188
 * @date 2021/2/4 11 :04
 * @description
 */
@Service
public class TokenServiceImpl  implements TokenService {

    @Autowired
    private TokenDAO tokenDAO;

    @Override
    public TokenEntity selectEntityByToken(String token) {
        return tokenDAO.selectEntityByToken(token);
    }

    @Override
    public TokenEntity selectEntityByUserId(Long userId) {
        return tokenDAO.selectEntityByUserId(userId);
    }

    @Override
    public void saveEntity(TokenEntity tokenEntity) {
        tokenDAO.saveEntity(tokenEntity);
    }

    @Override
    public void updateEntity(TokenEntity tokenEntity) {
        tokenDAO.updateEntity(tokenEntity);
    }

    @Override
    public void deleteEntity(Long id) {
        tokenDAO.deleteEntity(id);
    }

    @Override
    public String generateToken(Long userid, String username) {
        TokenEntity tokenEntity = this.selectEntityByUserId(userid);
        //生成token
        String token = CommonUtil.getRandomString(32);
        //时间生成
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        if(tokenEntity!=null){
            tokenEntity.setToken(token);
            tokenEntity.setExpiratedtime(calendar.getTime());
            this.updateEntity(tokenEntity);
        }else {
            TokenEntity newTokenEntity = new TokenEntity();
            newTokenEntity.setUserId(userid);
            newTokenEntity.setToken(token);
            newTokenEntity.setUserName(username);
            newTokenEntity.setExpiratedtime(calendar.getTime());
            this.saveEntity(newTokenEntity);
        }
        return token;
    }

    @Override
    public TokenEntity getTokenEntity(String token) {
        TokenEntity tokenEntity = this.selectEntityByToken(token);
        if(tokenEntity==null || tokenEntity.getExpiratedtime().getTime() < new Date().getTime()){
            return null;
        }
        return tokenEntity;
    }
}
