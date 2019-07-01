package com.robi.oauth;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.robi.util.HttpUtil;
import com.robi.util.JwtUtil;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class GoogleOAuth {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String OAUTH2_CODE_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String OAUTH2_TOKEN_URL = "https://www.googleapis.com/oauth2/v4/token";
    private static final String OAUTH2_CERTS_URL = "https://www.googleapis.com/oauth2/v3/certs";

    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_RESPONSE_TYPE = "response_type";
    private static final String KEY_SCOPE = "scope";
    private static final String KEY_NONCE = "nonce";
    private static final String KEY_REDIRECT_URI = "redirect_uri";
    private static final String KEY_STATE = "state";
    private static final String KEY_PROMPT = "prompt";
    private static final String KEY_DISPLAY = "display";
    private static final String KEY_LOGIN_HINT = "login_hint";
    private static final String KEY_ACCESS_TYPE = "access_type";
    private static final String KEY_INCLUDE_GRANTED_SCOPES = "include_granted_scopes";
    private static final String KEY_OPENID_REALM = "openid.realm";
    private static final String KEY_HD = "hd";
    private static final String KEY_CODE = "code";
    private static final String KEY_CLIENT_SECRET = "client_secret";
    private static final String KEY_GRANT_TYPE = "grant_type";

    public static final String STATE_OAUTH2_EMAIL = "https://www.googleapis.com/auth/userinfo.email";
    public static final String STATE_OAUTH2_PROFILE = "https://www.googleapis.com/auth/userinfo.profile";

    public static final String GRANT_TYPE = "authorization_code";

    public static PublicKey idTokenPublicKey = null;

    /**
     * <p>
     * < Google - OpenID Connect API ('code' from 'login') >
     * </p>
     * <p>
     * - Reference :
     * https://developers.google.com/identity/protocols/OpenIDConnect#authenticationuriparameters
     * </p>
     * <hr>
     **/
    public String makeUrlForCode(String clientId, String responseType, String scope, String nonce, String redirectUri,
            String state, String prompt, String display, String loginHint, String accessType,
            boolean includeGrantedScopes, String openidRealm, String hd) {
        // Required
        if (clientId == null || clientId.length() == 0) {
            logger.error("'clientId' is null or zero length! (clientId:" + clientId + ")");
            return null;
        }

        if (responseType == null || responseType.length() == 0) {
            logger.error("'responseType' is null or zero length! (responseType:" + responseType + ")");
            return null;
        }

        if (scope == null || scope.length() == 0) {
            logger.error("'scope' is null or zero length! (scope:" + scope + ")");
            return null;
        }

        if (nonce == null || nonce.length() == 0) {
            logger.error("'nonce' is null or zero length! (nonce:" + nonce + ")");
            return null;
        }

        if (redirectUri == null || redirectUri.length() == 0) {
            logger.error("'redirectUri' is null or zero length! (redirectUri:" + redirectUri + ")");
            return null;
        }

        StringBuilder rtSb = new StringBuilder(512);

        rtSb.append(OAUTH2_CODE_URL).append('?').append(KEY_CLIENT_ID).append('=').append(clientId).append('&')
                .append(KEY_RESPONSE_TYPE).append('=').append(responseType).append('&').append(KEY_SCOPE).append('=')
                .append(scope).append('&').append(KEY_NONCE).append('=').append(nonce).append('&')
                .append(KEY_REDIRECT_URI).append('=').append(redirectUri);

        // Recommended
        if (state != null && state.length() > 0) {
            rtSb.append('&').append(KEY_STATE).append('=').append(state);
        }

        // Optional
        if (prompt != null && prompt.length() > 0) {
            rtSb.append('&').append(KEY_PROMPT).append('=').append(prompt);
        }

        if (display != null && display.length() > 0) {
            rtSb.append('&').append(KEY_DISPLAY).append('=').append(display);
        }

        if (loginHint != null && loginHint.length() > 0) {
            rtSb.append('&').append(KEY_LOGIN_HINT).append('=').append(loginHint);
        }

        if (accessType != null && accessType.length() > 0) {
            rtSb.append('&').append(KEY_ACCESS_TYPE).append('=').append(accessType);
        }

        // rtSb.append('&').append(KEY_INCLUDE_GRANTED_SCOPES).append('=').append(includeGrantedScopes);

        if (openidRealm != null && openidRealm.length() > 0) {
            rtSb.append('&').append(KEY_OPENID_REALM).append('=').append(openidRealm);
        }

        if (hd != null && hd.length() > 0) {
            rtSb.append('&').append(KEY_HD).append('=').append(hd);
        }

        return rtSb.toString();
    }

    /**
     * <p>
     * < Google - OpenId Connect API ('id_token' from 'code') >
     * </p>
     * <p>
     * - Reference :
     * https://developers.google.com/identity/protocols/OpenIDConnect#server-flow
     * (ServerFlow#4)
     * </p>
     * <hr>
     */
    public Map<String, String> makePostDataForIdToken(String code, String clientId, String clientSecret,
            String redirectUri, String grantType) {
        if (code == null || code.length() == 0) {
            logger.error("'code' is null or zero length! (code:" + code + ")");
            return null;
        }

        if (clientId == null || clientId.length() == 0) {
            logger.error("'clientId' id null or zero length! (clientId:" + clientId + ")");
            return null;
        }

        if (clientSecret == null || clientSecret.length() == 0) {
            logger.error("'clientSecret' id null or zero length! (clientSecret:" + clientSecret + ")");
            return null;
        }

        if (redirectUri == null || redirectUri.length() == 0) {
            logger.error("'redirectUri' id null or zero length! (redirectUri:" + redirectUri + ")");
            return null;
        }

        if (grantType == null || grantType.length() == 0) {
            logger.error("'grantType' id null or zero length! (grantType:" + grantType + ")");
            return null;
        }

        Map<String, String> rtMap = new HashMap<String, String>();

        rtMap.put(KEY_CODE, code);
        rtMap.put(KEY_CLIENT_ID, clientId);
        rtMap.put(KEY_CLIENT_SECRET, clientSecret);
        rtMap.put(KEY_REDIRECT_URI, redirectUri);
        rtMap.put(KEY_GRANT_TYPE, grantType);

        return rtMap;
    }

    /**
     * <p>
     * < Google - OpenId Connect ('id_token' from 'code') >
     * </p>
     * <p>
     * - Reference :
     * https://developers.google.com/identity/protocols/OpenIDConnect#server-flow
     * (ServerFlow#4)
     * </p>
     * <hr>
     */
    public String getIdToken(String code, String clientId, String clientSecret, String redirectUri, String grantType) {
        Map<String, String> idTokenPostData = makePostDataForIdToken(code, clientId, clientSecret, redirectUri,
                grantType);

        if (idTokenPostData == null) {
            logger.error("'idTokenPostData' is null!");
            return null;
        }

        String[] resData = new String[1];

        HttpUtil.httpPost(OAUTH2_TOKEN_URL, null, idTokenPostData, null, resData);

        String rtIdToken = null;

        try {
            rtIdToken = new JSONObject(resData[0]).getString("id_token");
        } catch (JSONException e) {
            logger.error("Exception!", e);
        }

        return rtIdToken;
    }

    public Map<String, Object> verifyAndParseIdTokenJwt(String idToken) {
        if (idToken == null || idToken.length() == 0) {
            logger.error("'idToken' is null or zero length! (idToken:" + idToken + ")");
            return null;
        }

        Map<String, Object> rtMap = null;

        // Generate new key
        synchronized (this) { 
            if (idTokenPublicKey == null) { // for public key caching
                logger.info("Create new idToken public key!");
                
                // Get jwk_id(A.K.A 'kid') from idToken jwt for sign verify
                int headEndIdx = idToken.indexOf(".");

                if (headEndIdx == -1) {
                    logger.error("'idToken' is NOT complete jwt type!");
                    return null;
                }

                String base64JwtHeader = idToken.substring(0, headEndIdx);
                String keyId = null;

                try {
                    // Get 'keyId(kid)' from recv jwt's header
                    JSONObject jwtHeaderJson = new JSONObject(new String(Base64Utils.decodeFromUrlSafeString(base64JwtHeader)));
                    keyId = jwtHeaderJson.getString("kid");

                    if (keyId == null || keyId.length() == 0) {
                        logger.error("'keyId' is null or zero length! (kid:" + keyId + ")");
                        return null;
                    }

                    // GET url connection for public key 'E' and modulus 'N'
                    String[] jwkJsonStr = new String[1];

                    if (HttpUtil.httpGet(OAUTH2_CERTS_URL, null, null, jwkJsonStr) != HttpStatus.SC_OK) {
                        logger.error("httpGet to '" + OAUTH2_CERTS_URL + "' status code NOT 200!");
                        return null;
                    }

                    JSONObject jwkJsonObj = new JSONObject(jwkJsonStr[0]);
                    JSONArray jwkJsonKeyAry = jwkJsonObj.getJSONArray("keys");
                    int jwkKeyAryLen = jwkJsonKeyAry.length();
                    String keyE = null;
                    String modN = null;
                    
                    for (int i = 0; i < jwkKeyAryLen; ++i) {
                        try {
                            JSONObject jwtKeyObj = jwkJsonKeyAry.getJSONObject(i);
                            String jwkKidStr = jwtKeyObj.getString("kid");

                            if (jwkKidStr != null && jwkKidStr.equals(keyId)) {
                                keyE = jwtKeyObj.getString("e");
                                modN = jwtKeyObj.getString("n");
                                break;
                            }
                        }
                        catch (JSONException e) {
                            continue;
                        }
                    }

                    if (keyE == null || modN == null) {
                        logger.error("'keyE' or 'modN' is null! (keyE:" + keyE + ", modN:" + modN + ")");
                        return null;
                    }

                    if ((idTokenPublicKey = makePublicKey(modN, keyE)) == null) {
                        logger.error("'idTokenPublicKey' is null! FAIL to make publicKey!");
                        return null;
                    }
                }
                catch (JSONException e) {
                    logger.error("Exception!", e);
                    return null;
                }
            }
        }

        // Try decode idToken JWT
        try {
            rtMap = JwtUtil.parseJwt(idToken, null, idTokenPublicKey);
        }
        catch (Exception e) {
            synchronized (this) {
                idTokenPublicKey = null;
            }

            logger.error("Exception!", e);
            return null;
        }

        return rtMap;
    }

    // RSA 공개키 생성
    public PublicKey makePublicKey(String modBase64, String expBase64) {
        if (modBase64 == null || expBase64 == null) {
            logger.error("One of 'modBase64' or 'expBase64' is null! (modBase64:" + modBase64 + ", expBase64:" + expBase64 + ")");
            return null;
        }

        byte expAry[] = Base64.getUrlDecoder().decode(expBase64);
        byte modAry[] = Base64.getUrlDecoder().decode(modBase64);
        BigInteger exponent = new BigInteger(new String(Hex.encodeHex(expAry)), 16);
        BigInteger modulus = new BigInteger(new String(Hex.encodeHex(modAry)), 16);
        
        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory keyFactory = null;
        PublicKey publicKey = null;

        try {
            keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(rsaPublicKeySpec);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Exception!", e);
            return null;
        }

        return publicKey;
    }
}