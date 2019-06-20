package com.robi.oauth;

import java.util.Map;

import com.robi.util.HttpUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleOAuth {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String OAUTH2_CODE_URL  = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String OAUTH2_TOKEN_URL = "https://www.googleapis.com/oauth2/v4/token";

    private static final String KEY_CLIENT_ID               = "client_id";
    private static final String KEY_RESPONSE_TYPE           = "response_type";
    private static final String KEY_SCOPE                   = "scope";
    private static final String KEY_NONCE                   = "nonce";
    private static final String KEY_REDIRECT_URI            = "redirect_uri";
    private static final String KEY_STATE                   = "state";
    private static final String KEY_PROMPT                  = "prompt";
    private static final String KEY_DISPLAY                 = "display";
    private static final String KEY_LOGIN_HINT              = "login_hint";
    private static final String KEY_ACCESS_TYPE             = "access_type";
    private static final String KEY_INCLUDE_GRANTED_SCOPES  = "include_granted_scopes";
    private static final String KEY_OPENID_REALM            = "openid.realm";
    private static final String KEY_HD                      = "hd";
    private static final String KEY_CODE                    = "code";
    private static final String KEY_CLIENT_SECRET           = "client_secret";
    private static final String KEY_GRANT_TYPE              = "grant_type";

    public static final String STATE_OAUTH2_EMAIL   = "https://www.googleapis.com/auth/userinfo.email";
    public static final String STATE_OAUTH2_PROFILE = "https://www.googleapis.com/auth/userinfo.profile";

    public static final String GRANT_TYPE = "authorization_code";
    
    /**
     *  <p>< Google - OpenID Connect API ('code' from 'login') ></p>
     *  <p> - Reference : https://developers.google.com/identity/protocols/OpenIDConnect#authenticationuriparameters</p>
     *  <hr>
     **/
    public String makeUrlForCode(String clientId, String responseType, String scope,
                                 String nonce, String redirectUri, String state,
                                 String prompt, String display, String loginHint,
                                 String accessType, boolean includeGrantedScopes, String openidRealm,
                                 String hd) {
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
        
        rtSb.append(OAUTH2_CODE_URL).append('?').append(KEY_CLIENT_ID).append('=').append(clientId)
                                    .append('&').append(KEY_RESPONSE_TYPE).append('=').append(responseType)
                                    .append('&').append(KEY_SCOPE).append('=').append(scope)
                                    .append('&').append(KEY_NONCE).append('=').append(nonce)
                                    .append('&').append(KEY_REDIRECT_URI).append('=').append(redirectUri);

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

        rtSb.append('&').append(KEY_INCLUDE_GRANTED_SCOPES).append('=').append(includeGrantedScopes);

        if (openidRealm != null && openidRealm.length() > 0) {
            rtSb.append('&').append(KEY_OPENID_REALM).append('=').append(openidRealm);
        }

        if (hd != null && hd.length() > 0) {
            rtSb.append('&').append(KEY_HD).append('=').append(hd);
        }

        return rtSb.toString();
    }

    /**
     *  <p>< Google - OpenId Connect API ('id_token' from 'code') ></p>
     *  <p> - Reference : https://developers.google.com/identity/protocols/OpenIDConnect#server-flow (ServerFlow#4)</p>
     *  <hr>
     */
    public String makePostDataIdToken(String code, String clientId, String clientSecret, String redirectUri, String grantType) {
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

        StringBuilder rtSb = new StringBuilder(256);

        rtSb.append(KEY_CODE).append('=').append(code)
            .append('\n').append('&').append(KEY_CLIENT_ID).append('=').append(clientId)
            .append('\n').append('&').append(KEY_CLIENT_SECRET).append('=').append(clientSecret)
            .append('\n').append('&').append(KEY_REDIRECT_URI).append('=').append(redirectUri)
            .append('\n').append('&').append(KEY_GRANT_TYPE).append('=').append(grantType);

        return rtSb.toString();
    }

    /**
     *  <p>< Google - OpenId Connect ('id_token' from 'code') ></p>
     *  <p> - Reference : https://developers.google.com/identity/protocols/OpenIDConnect#server-flow (ServerFlow#4)</p>
     *  <hr>
     */
    public Map<String, Object> getIdToken(String code, String clientId, String clientSecret, String redirectUri, String grantType) {
        String idTokenPostData = makePostDataIdToken(code, clientId, clientSecret, redirectUri, grantType);

        if (idTokenPostData == null) {
            logger.error("'idTokenPostData' is null!");
            return null;
        }

        // HttpUtil.httpPost(OAUTH2_TOKEN_URL, null, idTokenPostData);

        return null;
    }
}