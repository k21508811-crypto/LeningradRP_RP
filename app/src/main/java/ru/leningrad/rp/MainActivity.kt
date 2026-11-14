package ru.leningrad.rp

import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(10,13,17)
        window.navigationBarColor = Color.rgb(10,13,17)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        setContentView(CityGameView(this))
    }
}
private class CityGameView(context: android.content.Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var x = 0f; private var y = 0f; private var angle = 0f
    private var inCar = false; private var speed = 0f; private var cash = 250; private var mission = 0
    private var dx = 0f; private var dy = 0f; private var gas = false; private var brake = false
    private var last = System.nanoTime()
    private val d = resources.displayMetrics.density
    private fun dp(v: Float) = v*d
    override fun onDraw(c: Canvas) {
        val dt=((System.nanoTime()-last)/1e9f).coerceIn(0f,.05f); last=System.nanoTime()
        if(inCar){ if(gas) speed=(speed+260*dt).coerceAtMost(430f) else speed*=.985f; if(brake)speed*=.9f
            angle+=dx*dt*2.2f; x+=sin(angle)*speed*dt; y-=cos(angle)*speed*dt
        } else { x+=dx*155f*dt; y+=dy*155f*dt }
        val tx=420f+mission*520f; val ty=-260f+mission*360f
        if(hypot(x-tx,y-ty)<85f){cash+=500;mission++}
        c.drawColor(Color.rgb(45,91,63))
        c.save(); c.translate(width/2f-x,height/2f-y); city(c); p.color=Color.rgb(255,196,45); c.drawCircle(tx,ty,32f,p)
        p.color=Color.BLACK;p.textSize=26f;p.textAlign=Paint.Align.CENTER;c.drawText("$",tx,ty+9,p)
        c.save();c.rotate(Math.toDegrees(angle.toDouble()).toFloat(),x,y)
        rect(c,Color.rgb(15,18,22),x-25,y-39,x+25,y+39,9f)
        rect(c,if(inCar)Color.rgb(210,45,45) else Color.rgb(40,145,230),x-19,y-32,x+19,y+32,7f)
        rect(c,Color.rgb(175,220,235),x-14,y-25,x+14,y-9,4f);c.restore();c.restore()
        hud(c);postInvalidateDelayed(16)
    }
    private fun rect(c:Canvas,col:Int,l:Float,t:Float,r:Float,b:Float,rad:Float=0f){p.color=col;c.drawRoundRect(l,t,r,b,rad,rad,p)}
    private fun city(c:Canvas){
        val left=x-width-700f;val right=x+width+700f;val top=y-height-700f;val bottom=y+height+700f
        var bx=floor(left/520f).toInt()*520f
        while(bx<right){var by=floor(top/520f).toInt()*520f
            while(by<bottom){
                rect(c,Color.rgb(65,69,72),bx,by,bx+520,by+520)
                rect(c,Color.rgb(42,45,48),bx+175,by,bx+345,by+520)
                rect(c,Color.rgb(42,45,48),bx,by+175,bx+520,by+345)
                rect(c,Color.rgb(113,119,124),bx+28,by+28,bx+158,by+158,8f)
                rect(c,Color.rgb(75,86,96),bx+40,by+40,bx+146,by+146,5f)
                rect(c,Color.rgb(139,125,105),bx+368,by+368,bx+492,by+492,8f)
                rect(c,Color.rgb(76,95,105),bx+380,by+380,bx+480,by+480,5f)
                by+=520f
            };bx+=520f
        }
    }
    private fun hud(c:Canvas){
        rect(c,0xDD10151B.toInt(),dp(12f),dp(14f),width-dp(12f),dp(96f),dp(14f))
        p.color=Color.WHITE;p.textAlign=Paint.Align.LEFT;p.textSize=dp(17f);c.drawText("GRAND CITY 5",dp(25f),dp(42f),p)
        p.color=Color.rgb(255,196,45);p.textSize=dp(14f);c.drawText("$ $cash",dp(25f),dp(70f),p)
        p.color=Color.WHITE;p.textAlign=Paint.Align.RIGHT;c.drawText("МИССИЯ "+(mission+1),width-dp(24f),dp(57f),p)
        p.color=0x668FFFFFF.toInt();c.drawCircle(dp(92f),height-dp(112f),dp(62f),p)
        p.color=0x99FFFFFF.toInt();c.drawCircle(dp(92f)+dx*dp(25f),height-dp(112f)+dy*dp(25f),dp(25f),p)
        button(c,width-dp(94f),height-dp(190f),"ГАЗ",gas);button(c,width-dp(94f),height-dp(105f),if(inCar)"ВЫЙТИ" else "МАШИНА",false);button(c,width-dp(94f),height-dp(30f),"ТОРМОЗ",brake)
        p.color=Color.WHITE;p.textSize=dp(11f);p.textAlign=Paint.Align.CENTER;c.drawText("Доберись до жёлтой метки",width/2f,height-dp(18f),p)
    }
    private fun button(c:Canvas,cx:Float,cy:Float,label:String,active:Boolean){
        p.color=if(active)0xDDC93636.toInt() else 0xBB151B22.toInt()
        c.drawRoundRect(cx-dp(64f),cy-dp(29f),cx+dp(64f),cy+dp(29f),dp(15f),dp(15f),p)
        p.color=Color.WHITE;p.textSize=dp(12f);p.textAlign=Paint.Align.CENTER;c.drawText(label,cx,cy+dp(4f),p)
    }
    override fun onTouchEvent(e:MotionEvent):Boolean{
        val w=width.toFloat();val h=height.toFloat()
        when(e.actionMasked){
            MotionEvent.ACTION_DOWN,MotionEvent.ACTION_POINTER_DOWN,MotionEvent.ACTION_MOVE->{
                dx=0f;dy=0f;gas=false;brake=false
                for(i in 0 until e.pointerCount){val tx=e.getX(i);val ty=e.getY(i)
                    if(tx<w*.48f&&ty>h*.55f){dx=((tx-dp(92f))/dp(55f)).coerceIn(-1f,1f);dy=((ty-(h-dp(112f)))/dp(55f)).coerceIn(-1f,1f)}
                    else if(tx>w*.55f){if(ty>h-dp(65f))brake=true else if(ty>h-dp(145f)){inCar=!inCar;speed=0f}else gas=true}
                }
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL,MotionEvent.ACTION_POINTER_UP->{dx=0f;dy=0f;gas=false;brake=false}
        };return true
    }
}