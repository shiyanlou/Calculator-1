package com.example.shiyanlou.calculator;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import org.wltea.expression.ExpressionEvaluator;
public class CalculatorActivity extends Activity{
    // 界面的主要的两个控件
    private GridView mGridButtons = null;
    private EditText mEditInput = null;
    // 适配器
    private BaseAdapter mAdapter = null;
    // EditText显示的内容，mPreStr表示灰色表达式部分，要么为空，要么以换行符结尾
    private String mPreStr = "";
    // mLastStr表示显示内容的深色部分
    private String mLastStr = "";
    /**
     * 这个变量非常重要，用于判断是否是刚刚成功执行完一个表达式
     * 因为，新加一个表达式的时候，需要在原来的表达式后面加上换行标签等
     */
    private boolean mIsExcuteNow = false;
    // html换行的标签
    private final String newLine = "<br\\>";
    // gridview的所有按钮对应的键的内容
    private final String[] mTextBtns = new String[]{
            "Back","(",")","CE",
            "7","8","9","/",
            "4","5","6","*",
            "1","2","3","+",
            "0",".","=","-",};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏，需要在setContentView之前调用
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_calculator);
        // 查找控件
        mGridButtons = (GridView) findViewById(R.id.grid_buttons);
        mEditInput = (EditText) findViewById(R.id.edit_input);
        // 新建adpater对象，并给GridView设置适配器
        mAdapter = new CalculatorAdapter(this, mTextBtns);
        mGridButtons.setAdapter(mAdapter);
        // 这句话的目的是为了让EditText不能从键盘输入
        mEditInput.setKeyListener(null);
        // 新建一个自定义AdapterView.OnItemClickListener的对象，用于设置GridView每个选项按钮点击事件
        OnButtonItemClickListener listener = new OnButtonItemClickListener();
        mGridButtons.setOnItemClickListener(listener);
    }
    /**
     * 这个函数用于设置EditText的显示内容，主要是为了加上html颜色标签。
     * 所有的显示EditText内容都需要调用此函数
     */
    private void setText(){
        final String[] tags = new String[]{"<font color='#858585'>", "<font color='#CD2626'>", "</font> "};
        StringBuilder builder = new StringBuilder();
        // 添加颜色标签
        builder.append(tags[0]); builder.append(mPreStr); builder.append(tags[2]);
        builder.append(tags[1]); builder.append(mLastStr); builder.append(tags[2]);
        mEditInput.setText(Html.fromHtml(builder.toString()));
        mEditInput.setSelection(mEditInput.getText().length());
        // 表示获取焦点
        mEditInput.requestFocus();
    }
    /**
     * 当用户按下 = 号的时候，执行的函数
     * 用于执行当前表达式，并判断是否有错误
     */
    private void excuteExpression(){
        Object result = null;
        try{
            // 第三方包执行表达是的调用
            result = ExpressionEvaluator.evaluate(mLastStr);
        }catch (Exception e){
            // 如果捕获到异常，表示表达式执行失败，调用setError方法显示错误信息
            //Toast.makeText(this, "表达式解析错误，请检查!", Toast.LENGTH_SHORT).show();
            mEditInput.setError(e.getMessage());
            mEditInput.requestFocus();
            // 这里设置为false是因为并没有执行成功，还不能开始新的表达式求值
            mIsExcuteNow=false;
            return;
        }
        // 执行成功了，设置标志为true，同时更新最后的表达式的内容为 表达式 + '=' + result
        mIsExcuteNow = true;
        mLastStr += "="+result;
        mEditInput.setError(null);
        // 显示执行结果
        setText();
    }
    /**
     * 该类是自定义选项按钮单击事件监听器
     */
    private class OnButtonItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String text = (String) parent.getAdapter().getItem(position);
            if(text.equals("=")){
                // 为 = 号时直接调用该方法就好
                excuteExpression();
            }else if(text.equals("Back")){
                // 如果按下退格按钮，表示删除一个字符
                // 如果最新的表达式长度为0，则需要把前面的表达式的最后部分赋值给最新的表达式
                if(mLastStr.length() == 0){
                    // 如果历史表达式的长度不是0，那么此时历史表达式必然以换行符结尾
                    // 如 3+5=8<br/>
                    if(mPreStr.length() != 0){
                        // 此时首先清除mPreStr的末尾的换行符 即 3+5=8
                        mPreStr = mPreStr.substring(0, mPreStr.length()-newLine.length());
                        mIsExcuteNow = true;
                        // 找到前一个换行符的位置
                        int index = mPreStr.lastIndexOf(newLine);
                        if(index == -1){
                            // 表示没有找到，即历史表达式只有一个 3+5=8不含有换行符就表示没有找到
                            mLastStr = mPreStr;
                            mPreStr = "";
                        }else{
                            // 找到了的话，就把历史表达式的最后一个表达式赋值给
                            // 比如历史表达式为3=3<br/>3+5=8此时，就需要吧3+5=8作为最新表达式
                            mLastStr = mPreStr.substring(index+newLine.length(), mPreStr.length());
                            mPreStr = mPreStr.substring(0, index);
                        }
                    }
                }else{
                    // 如果最新的表达式长度不是0，则直接减掉一个字符就好了
                    mLastStr = mLastStr.substring(0, mLastStr.length()-1);
                }
                // 显示内容
                setText();
            }else if(text.equals("CE")){
                // 需要全被设置为空字符串，并设置标识为false，同时清空显示内容
                mPreStr = "";
                mLastStr = "";
                mIsExcuteNow = false;
                mEditInput.setText("");
            }else{
                // 按下其它键的情况
                if(mIsExcuteNow){
                    // 如果刚刚成功执行了一个表达式，那么需要把当前表达式加到历史表达式后面并添加换行符
                    mPreStr += mLastStr + newLine;
                    // 重置标识为false
                    mIsExcuteNow = false;
                    // 设置最新表达式的第一个字符为当前按钮按下的内容
                    mLastStr = text;
                }else{
                    // 否则直接在最新表达式后面添加内容就好了
                    mLastStr += text;
                }
                // 更新内容
                setText();
            }
        }
    }
}