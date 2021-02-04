package com.wzqcode.service;

import com.wzqcode.entity.TokenEntity;

/**
 * @author 14188
 * @date 2021/2/4 10 :33
 * @description
 */
public interface TokenService {

    TokenEntity selectEntityByToken(String token);

    TokenEntity selectEntityByUserId(Long userId);

    void saveEntity(TokenEntity tokenEntity);

    void updateEntity(TokenEntity tokenEntity);

    void deleteEntity(Long id);

    String generateToken(Long userid, String username);

    TokenEntity getTokenEntity(String token);
}
