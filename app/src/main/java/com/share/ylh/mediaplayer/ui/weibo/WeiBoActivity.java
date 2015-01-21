package com.share.ylh.mediaplayer.ui.weibo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.share.ylh.mediaplayer.R;
import com.share.ylh.mediaplayer.base.BaseApp;
import com.sina.weibo.sdk.WeiboAppManager;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;

import java.text.SimpleDateFormat;

/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2015-01-17
 * Time: 12:43
 * FIXME
 */
public class WeiBoActivity extends Activity  implements View.OnClickListener, IWeiboHandler.Response  {

    /** 微博API接口类，提供登陆等功能  */
   // private Weibo mWeibo;
    private AuthInfo mAuthInfo;

    /** 微博微博分享接口实例 */
    private IWeiboShareAPI mWeiboShareAPI = null;

    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;

    // 2. 初始化从第三方到微博的消息请求
    SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.weibo);

        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);

        // 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
        // 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
        // NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
        mWeiboShareAPI.registerApp();

        // 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），
        // 需要调用 {@link IWeiboShareAPI#handleWeiboResponse} 来接收微博客户端返回的数据。
        // 执行成功，返回 true，并调用 {@link IWeiboHandler.Response#onResponse}；
        // 失败返回 false，不调用上述回调
        if (savedInstanceState != null) {
            mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
        }

        AuthInfo authInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(getApplicationContext());
        String token = "";
        if (accessToken != null) {
            token = accessToken.getToken();
        }

        mWeiboShareAPI.sendRequest(this, request, authInfo, token, new WeiboAuthListener() {

            @Override
            public void onWeiboException( WeiboException arg0 ) {
            }

            @Override
            public void onComplete( Bundle bundle ) {
                // TODO Auto-generated method stub
                Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                AccessTokenKeeper.writeAccessToken(getApplicationContext(), newToken);
                Toast.makeText(getApplicationContext(), "onAuthorizeComplete token = " +
                        newToken.getToken(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
            }
        });
//        findViewById(R.id.loginBySina).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("test", "on click");
//
//                mWeibo.anthorize(WeiBoActivity.this, new AuthDialogListener());
//            }
//        });


        mAccessToken = AccessTokenKeeper.readAccessToken(this);  //获取Token信息（如果上次授权成功，并保存了Token信息，那本本次可以直接使用本方式获取Token信息）
        if (mAccessToken.isSessionValid()) {    //判断是否登录成功
            String date = new java.text.SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
                    .format(new java.util.Date(mAccessToken.getExpiresTime()));
            String info = "access_token 仍在有效期内,无需再次登录: \naccess_token:"
                    + mAccessToken.getToken() + "\n有效期：" + date ;
            Toast.makeText(WeiBoActivity.this, info, Toast.LENGTH_LONG).show() ;
        }

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onResponse(BaseResponse baseResponse) {
        switch (baseResponse.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                Toast.makeText(this, R.string.weibosdk_demo_toast_share_success, Toast.LENGTH_LONG).show();
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                Toast.makeText(this, R.string.weibosdk_demo_toast_share_canceled, Toast.LENGTH_LONG).show();
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                Toast.makeText(this,
                        getString(R.string.weibosdk_demo_toast_share_failed) + "Error Message: " + baseResponse.errMsg,
                        Toast.LENGTH_LONG).show();
                break;
        }
    }


    class AuthDialogListener implements WeiboAuthListener {

        /**
         * 授权成功后会触发该方法，并且会将授权的信息放入Bundle中
         */
        @SuppressLint("SimpleDateFormat")
        @Override
        public void onComplete(Bundle values) {

            String token = values.getString("access_token");
            String expires_in = values.getString("expires_in");
            mAccessToken = new Oauth2AccessToken(token, expires_in);
            if (mAccessToken.isSessionValid()) {
                String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                        .format(new java.util.Date(mAccessToken.getExpiresTime()));
                String info = "认证成功: \r\n access_token: " + token + "\r\n" + "expires_in: "
                        + expires_in + "\r\n有效期：" + date;
                AccessTokenKeeper.writeAccessToken(WeiBoActivity.this, mAccessToken); //将登录信息保存，下次可以直接获取
                Log.i("test", info);
                Toast.makeText( WeiBoActivity.this , info , Toast.LENGTH_LONG).show() ;
            }else{
                Toast.makeText(WeiBoActivity.this, "认证失败", Toast.LENGTH_SHORT).show();
            }
        }

//        @Override
//        public void onError(WeiboDialogError e) {
//            Toast.makeText(getApplicationContext(),
//                    "Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }

        @Override
        public void onCancel() {
            Toast.makeText(getApplicationContext(), "Auth cancel", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(getApplicationContext(),
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
