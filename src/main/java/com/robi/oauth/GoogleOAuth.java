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

        // Get jwk_id(A.K.A 'kid') from idToken jwt for sign verify
        int headEndIdx = idToken.indexOf(".");

        if (headEndIdx == -1) {
            logger.error("'idToken' is NOT complete jwt type!");
            return null;
        }

        String base64JwtHeader = idToken.substring(0, headEndIdx);
        String keyId = null;

        try {
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

            String jwtKidStr = jwkJsonStr[0];

            if (jwtKidStr == null || jwtKidStr.length() == 0) {
                logger.error("'jwtKidStr' is null or zero length! (kid:" + jwtKidStr + ")");
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

                    if (jwkKidStr != null && jwkKidStr.equals(jwtKidStr)) {
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

            // 여기부터 계속....
            // keyE와 modN 값을 사용하여 verify 공개키 생성, JwtUtil에 넘김...
            // 아래 주석부분 참고할 것! @@
        }
        catch (JSONException e) {
            logger.error("Exception!", e);
        }
       
        
        return null;
    }
/*
        final String idTokenKey = "\"id_token\": ";
        int idTokenBgnIdx = resData[0].indexOf(idTokenKey) + idTokenKey.length() + 1;
        int idTokenEndIdx = resData[0].indexOf("\"", idTokenBgnIdx);
        String idTokenJwt = resData[0].substring(idTokenBgnIdx, idTokenEndIdx);

        logger.info("idTokenBgnIdx : " + idTokenBgnIdx);
        logger.info("idTokenEndIdx : " + idTokenEndIdx);
        logger.info("idTokenJwt : " + idTokenJwt);

        if (idTokenJwt == null || idTokenJwt.length() == 0) {
            logger.error("'idTokenJwt' is null or zero length! (idTokenJwt:" + idTokenJwt + ")");
            return null;
        }

        //// test /////////////////////////////////////
        String exponentB64u = "AQAB";
        String modulusB64u = "rgPFxdeuvEf8c4PePDc3mNuG2pM1_OfoWNQmVFg7iyc1VBnjhEDFcynhpL3vy30PfwGse9LcRbTVQUNnWjnWNt09XAmuYKWQc-jpad_M_G1Vd3gHCGmPAo1bG7iu3XTYIT3sm-PuI-aKHcee7H76f0MxFfdZuOEw7l__pjTDtv0Md914cJbq7Egk8pAk4PWv0FI9DRqnVLCDR0r4rCJb4Dq_hNPbFJMXxUyxvLYp9Nq2BaCQN4ZuS910GQTpLHBkOkXtZWCy__vrtBg5cshG3ASZmCtCH8InsQugCpKkVXIEUuEwJC3bzxOaXGjXe32G0zsUH789s7BuIP-EGrt1fQ";

        //Build the public key from modulus and exponent
        try {
        PublicKey publicKey = getPublicKey (modulusB64u,exponentB64u);

        //print key as PEM (base64 and headers)
        String publicKeyPEM = 
              "-----BEGIN PUBLIC KEY-----\n" 
            + Base64.getEncoder().encodeToString(publicKey.getEncoded()) +"\n"
            + "-----END PUBLIC KEY-----";

            Map<String, Object> rtMap = JwtUtil.parseJwt(idTokenJwt, null, publicKey);

            for (String key : rtMap.keySet()) {
                logger.info(key + " : " + rtMap.get(key));
            }
        }
        catch (Exception e) {
            logger.error("Exception!", e);
        }

        return null;*/

    private static final String[] HEX_TABLE = new String[]{
        "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0a", "0b", "0c", "0d", "0e", "0f",
        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1a", "1b", "1c", "1d", "1e", "1f",
        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2a", "2b", "2c", "2d", "2e", "2f",
        "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3a", "3b", "3c", "3d", "3e", "3f",
        "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4a", "4b", "4c", "4d", "4e", "4f",
        "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5a", "5b", "5c", "5d", "5e", "5f",
        "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6a", "6b", "6c", "6d", "6e", "6f",
        "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7a", "7b", "7c", "7d", "7e", "7f",
        "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8a", "8b", "8c", "8d", "8e", "8f",
        "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9a", "9b", "9c", "9d", "9e", "9f",
        "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "aa", "ab", "ac", "ad", "ae", "af",
        "b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8", "b9", "ba", "bb", "bc", "bd", "be", "bf",
        "c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "ca", "cb", "cc", "cd", "ce", "cf",
        "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "da", "db", "dc", "dd", "de", "df",
        "e0", "e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8", "e9", "ea", "eb", "ec", "ed", "ee", "ef",
        "f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "fa", "fb", "fc", "fd", "fe", "ff",
    };

    public static String toHexFromBytes(byte[] bytes) {
        StringBuffer rc = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            rc.append(HEX_TABLE[0xFF & bytes[i]]);
        }
        return rc.toString();
    }

    // Build the public key from modulus and exponent
    public static PublicKey getPublicKey (String modulusB64u, String exponentB64u) throws NoSuchAlgorithmException, InvalidKeySpecException{
        //conversion to BigInteger. I have transformed to Hex because new BigDecimal(byte) does not work for me
        byte exponentB[] = Base64.getUrlDecoder().decode(exponentB64u);
        byte modulusB[] = Base64.getUrlDecoder().decode(modulusB64u);
        BigInteger exponent = new BigInteger(toHexFromBytes(exponentB), 16);
        BigInteger modulus = new BigInteger(toHexFromBytes(modulusB), 16);

        //Build the public key
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey pub = factory.generatePublic(spec);

        return pub;
    }
}