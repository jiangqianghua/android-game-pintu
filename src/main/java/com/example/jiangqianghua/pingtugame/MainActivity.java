package com.example.jiangqianghua.pingtugame;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ImageView[][] iv_game_arr = new ImageView[3][5];
    private GridLayout gl_main_game ;
    /** 当前空房快的实例*/
    private ImageView iv_null_image ;
    private GestureDetector mDetector ;
    private boolean isGameOver = false;

    private boolean isGameStart = false ;
    private boolean isAnimRun = false ;
    private static final String TAG = "MainActivity";
    private int ivWandW = 0 ;
    private int ivWandH = 0 ;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 获取一章大图，切成小图片
        Bitmap bigBm = ((BitmapDrawable)getResources().getDrawable(R.drawable.image)).getBitmap();
        int tuWandH = bigBm.getHeight()/ iv_game_arr.length;
        int tuWandW = bigBm.getWidth()/iv_game_arr[0].length ;

        ivWandW = getWindowManager().getDefaultDisplay().getWidth()/5;// 显示宽度，撑满宽度
        float scaleWH = (float) (tuWandW*1.0/tuWandH*1.0);
        Log.d(TAG, "onCreate: "+scaleWH);
        ivWandH = (int) (ivWandW*1.0 /scaleWH);
        for(int i = 0 ; i < iv_game_arr.length ; i++){
            for(int j = 0 ; j < iv_game_arr[0].length ; j++){
                Bitmap bm = Bitmap.createBitmap(bigBm ,j*tuWandW,i*tuWandH , tuWandW , tuWandH );
                iv_game_arr[i][j] = new ImageView(this);
                iv_game_arr[i][j].setImageBitmap(bm);
                iv_game_arr[i][j].setLayoutParams(new RelativeLayout.LayoutParams(ivWandW,ivWandH));
                // 设置间距
                iv_game_arr[i][j].setPadding(2,2,2,2);
                //  绑定自定义的数据
                iv_game_arr[i][j].setTag(new GameData(i,j,bm));
                iv_game_arr[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean flag = isHasByNULLImageView((ImageView) v);
                        //Toast.makeText(MainActivity.this,"位置关系是:"+flag,Toast.LENGTH_SHORT).show();
                        if(flag)
                        {
                            changeDataByImageView((ImageView)v);
                        }
                    }
                });
            }
        }

        gl_main_game = (GridLayout) findViewById(R.id.gl_main_game);
        for(int i = 0 ; i < iv_game_arr.length ; i++){
            for(int j = 0 ; j < iv_game_arr[0].length ; j++){
                gl_main_game.addView(iv_game_arr[i][j]);
            }
        }

        // 设置某个方块为空
        setNULLImageView(iv_game_arr[2][4]);
        randomMove();//  随机打乱顺序
        isGameStart = true ;
        // 手势
        mDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                int type = getDirByGes(e1.getX(),e1.getY(),e2.getX(),e2.getY()) ;
               // Toast.makeText(MainActivity.this,tag+"",Toast.LENGTH_SHORT).show();
                changeByDir(type);
                return false;
            }
        });
    }

    public void setNULLImageView(ImageView mImageView)
    {
        mImageView.setImageBitmap(null);
        iv_null_image = mImageView ;
    }
    public void changeByDir(int type){
        changeByDir(type,true);
    }
    // 根据手势方向，获取空方块对应的相邻的位置，那么数据交换
    /**
     * 1 上，2 下，3 左 ，4 右
     * @param type
     * @param isAnim 是否有动画
     */
    public void changeByDir(int type,boolean isAnim){
        //  获取当前空方向的位置
        GameData mNULLGamneData = (GameData) iv_null_image.getTag();
        // 根据方向，设置相应的相邻的坐标
        int new_x = mNULLGamneData.x ;
        int new_y = mNULLGamneData.y ;
        if(type == 1){
            new_x++ ;
        }else if(type == 2){
            new_x-- ;
        }else if(type == 3) {
            new_y++;
        }else if(type == 4){
            new_y--;
        }
        // 判断坐标是否存在
        if(new_x >= 0 && new_x < iv_game_arr.length && new_y >= 0 && new_y < iv_game_arr[0].length) {
            // 存在开始移动
            if(isAnim) {
                changeDataByImageView(iv_game_arr[new_x][new_y]);
            }
            else
            {
                changeDataByImageView(iv_game_arr[new_x][new_y],false);
            }
        }else
        {

        }
    }
    /**
     * 手势判断，是向左还是右滑动
     * @param start_x
     * @param start_y
     * @param end_x
     * @param end_y
     * @return 1 上，2 下，3 左 ，4 右
     */
    public int getDirByGes(float start_x , float start_y ,float end_x , float end_y)
    {
        // 判断是否是左右结构
        boolean isLeftOrRight = (Math.abs(start_x-end_x)>Math.abs(start_y-end_y))?true:false;
        if(isLeftOrRight)
        {
            boolean isLeft = start_x - end_x > 0?true:false;
            if(isLeft){
                return 3 ;
            } else {
                return 4 ;
            }
        }
        else
        {
            boolean isUp = start_y - end_y > 0?true:false;
            if(isUp){
                return 1 ;
            } else {
                return 2 ;
            }
        }
    }

    /**
     * 打乱图片顺序
     */
    public void randomMove(){
        //打乱次数
        for(int i = 0 ; i < 100 ; i++)
        {
            int type = (int) ((Math.random()*4) + 1);
            changeByDir(type,false);
        }
        //根据手势开始变化
    }

    public void changeDataByImageView(final ImageView mImageview){
        changeDataByImageView(mImageview , true);
    }

    // 利用动画结束后，交换两个方块位置
    public void changeDataByImageView(final ImageView mImageview,boolean isAnim){

        if(isAnimRun)
        {
            return ;
        }
        if(!isAnim)
        {
            GameData mGameData = (GameData) mImageview.getTag();
            GameData mNULLGameData = (GameData) iv_null_image.getTag();
            iv_null_image.setImageBitmap(mGameData.bm);
            mNULLGameData.bm = mGameData.bm ;
            mNULLGameData.p_x = mGameData.p_x ;
            mNULLGameData.p_y = mGameData.p_y ;
            // 设置当前点击的是空方块
            setNULLImageView(mImageview);
            if(isGameStart)
                isGameOver();
            return ;
        }
        //  创建动画
        TranslateAnimation translateAnimation = null ;
        if(mImageview.getX() > iv_null_image.getX()) //当前方块在空方块的下边
        {
            translateAnimation = new TranslateAnimation(0.1f,-ivWandH,0.1f,0.1f);
        }else  if(mImageview.getX() < iv_null_image.getX()) //当前方块在空方块的上边
        {
            translateAnimation = new TranslateAnimation(0.1f,ivWandH,0.1f,0.1f);
        }
        else  if(mImageview.getY() > iv_null_image.getY()) //当前方块在空方块的右边
        {
            translateAnimation = new TranslateAnimation(0.1f,0.1f,0.1f,-ivWandW);
        }
        else  if(mImageview.getY() < iv_null_image.getY()) //当前方块在空方块的左边
        {
            translateAnimation = new TranslateAnimation(0.1f,0.1f,0.1f,ivWandW);
        }
        // 动画时长
        translateAnimation.setDuration(70);
        // 设置动画结束后是否停留
        translateAnimation.setFillAfter(true);
        // 设置动画结束数据交换
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimRun = true ;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mImageview.clearAnimation();
                GameData mGameData = (GameData) mImageview.getTag();
                GameData mNULLGameData = (GameData) iv_null_image.getTag();
                iv_null_image.setImageBitmap(mGameData.bm);
                mNULLGameData.bm = mGameData.bm ;
                mNULLGameData.p_x = mGameData.p_x ;
                mNULLGameData.p_y = mGameData.p_y ;
                // 设置当前点击的是空方块
                setNULLImageView(mImageview);
                if(isGameStart)
                    isGameOver();
                isAnimRun = false ;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // 制定当前动画
        mImageview.startAnimation(translateAnimation);
    }
    /**
     * 判断当前方块点击的方块是否与空方块位置关系是相邻
     */
    public boolean isHasByNULLImageView(ImageView mImageView){
        GameData mNullGameData = (GameData) iv_null_image.getTag();
        GameData mGameData = (GameData) mImageView.getTag();
        //分别获取
        if(mNullGameData.y == mGameData.y && mNullGameData.x == mGameData.x + 1){   //  空方块在当前点击方块的上层
            return true ;
        }else if(mNullGameData.y == mGameData.y && mNullGameData.x == mGameData.x - 1){ //  空方块在当前点击方块的下层
            return true ;
        }else if(mNullGameData.y == mGameData.y + 1 && mNullGameData.x == mGameData.x ){ //  空方块在当前点击方块的左层
            return true ;
        }else if(mNullGameData.y == mGameData.y - 1 && mNullGameData.x == mGameData.x){ //  空方块在当前点击方块的右层
            return true ;
        }
        return false ;
    }


    public void  isGameOver(){
        isGameOver = true ;
        // 循环每个方块
        for(int i = 0 ; i < iv_game_arr.length ; i++) {
            for (int j = 0; j < iv_game_arr[0].length; j++) {
                // 为空的数据跳过
                if(iv_game_arr[i][j] == iv_null_image){
                    continue;
                }
                GameData mGameData = (GameData) iv_game_arr[i][j].getTag();
                if(!mGameData.isTrue()){
                    isGameOver = false ;
                    break;
                }
                // 根据一个开关变量决定游戏是否结束
            }
        }

        if(isGameOver){
            Toast.makeText(this,"game over",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 每个游戏消方块要绑定的数据
     */
    class GameData
    {
        /** 每个小方块实际的位置x,y*/
        public int x = 0;
        public int y = 0;
        /** 每个小方块的图片*/
        public Bitmap bm;
        /** 每个消方块的图片位置*/
        public int p_x = 0;
        public int p_y = 0;


        public GameData( int x, int y, Bitmap bm){
            super();
            this.x = x;
            this.y = y;
            this.bm = bm ;
            this.p_x = x ;
            this.p_y = y ;
        }

        /**
         * 判断方块位置是否正确
         * @return
         */
        public boolean isTrue(){
            return this.x == this.p_x && this.y == this.p_y ;
        }
    }

}
