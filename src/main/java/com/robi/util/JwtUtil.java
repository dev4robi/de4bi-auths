package com.robi.util;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.security.SignatureException;

public class JwtUtil {    
    // [Class private constants]
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // [Methods]
    /**
     * <p>서명된 JWT(Jasson Web Token)을 생성합니다.</p>
     * @param jwtClaims : JWT Body.
     * <pre>
     * - ID(jti) : JWT고유 ID
     * - Subject(sub) : 내용 제목
     * - Audience(aud) : 대상자
     * - Issure(iss) : 발행자
     * - IssuedAt(iat) : 발행시간
     * - Expiration(exp) : 만료시간
     * - NotBefore(nbf) : 유효 시작시간</pre>
     * @param signKey : 서명 키.
     * @return 생성된 JWT 문자열.
     * @see https://github.com/jwtk/jjwt
     */
    public static String buildJwt(Map<String, Object> jwtHeader, Map<String, Object> jwtClaims, Key signKey) {
        try {
            // 파라미터 검사
            if (signKey == null) {
                logger.error("'signKey' is null! (signKey:" + signKey + ")");
                return null;
            }
            
            // [Header]
            // - setHeader(Map<String, Object>)
            // [Claims]
            // - setClaims(Map<String, Object>)
            // - setId(String) : 'jti'
            // - setSubject(String) : 'sub'
            // - setAudience(String) : 'aud'
            // - setIssuer(String) : 'iss'
            // - setIssuedAt(String) : 'iat'
            // - setExpiration(Date) : 'exp'
            // - setNotBefore(Date) : 'nbf'
            
            // 난수패딩 헤더 추가
            Map<String, Object> headerMap = new HashMap<String, Object>();
            headerMap.put("pad", RandomUtil.genRandomStr(16, RandomUtil.ALPHABET | RandomUtil.NUMERIC));

            if (jwtHeader != null) {
                headerMap.putAll(jwtHeader);
            }
            
            // JWT 생성하여 반환
            return Jwts.builder().setHeader(headerMap).setClaims(jwtClaims).signWith(signKey).compact();
        }
        catch (Exception e) {
            logger.error("Util Exception!", e);
            return null;
        }
    }
    
    /**
     * <p>JWT(Jasson Web Token)를 파싱하여 데이터를 Map으로 반환합니다.</p>  
     * @param jwtStr : JWT 문자열.
     * @param jwtReqClaims : Claims에 필수로 존재해야 하는 key,value 쌍.
     * @param signKey : 서명 키.
     * @throws IllegalArgumentException jwtStr가 null이거나 길이가 0인경우.
     * @throws MalformedJwtException jwtStr가 JWT토큰 포멧이 아닌경우.
     * @throws ExpiredJwtException 토큰 유효기간이 만료된 경우.
     * @throws SignatureException 서명검사 오류가 발생한 경우.
     * @throws MissingClaimException jwtRequried의 key값이 Claims에 존재하지 않는 경우.
     * @throws IncorrectClaimException jwtRequried의 key값에 해당하는 value가 불일치하는 경우.
     * @return 생성된 JWT 문자열.
     * @see https://github.com/jwtk/jjwt
     */
    public static Map<String, Object> parseJwt(String jwtStr, Map<String, Object> jwtReqClaims, Key signKey) 
        throws IllegalArgumentException, MalformedJwtException, ExpiredJwtException,
               SignatureException, MissingClaimException, IncorrectClaimException {
        // 파라미터 검사
        if (jwtStr == null || jwtStr.length() == 0) {
            logger.error("'jwtStr' is null or zero length! (jwtStr:" + jwtStr + ")");
            return null;
        }
        
        // JWT 파싱 시작
        JwtParser jwtParser = Jwts.parser();
        
        // Header, Claims 필수값 검사
        if (jwtReqClaims != null) {
            for (String key : jwtReqClaims.keySet()) {
                jwtParser = jwtParser.require(key, jwtReqClaims.get(key));
            }
        }
        
        // 서명키 등록
        if (signKey != null) {
            jwtParser = jwtParser.setSigningKey(signKey);
        }
        
        // Claims 추출
        Jws<Claims> jws = jwtParser.parseClaimsJws(jwtStr);
        Claims claims = jws.getBody();

        // HashMap에 담아서 반환
        Map<String, Object> rtClaimsMap = new HashMap<String, Object>();
        
        for (String key : claims.keySet()) {
            rtClaimsMap.put(key, claims.get(key));
        }
        
        return rtClaimsMap;
    }
}
