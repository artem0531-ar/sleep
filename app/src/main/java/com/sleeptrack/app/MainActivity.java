package com.sleeptrack.app;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class MainActivity extends Activity {
    private final int BG=Color.rgb(10,17,14), TEXT=Color.rgb(241,245,240), MUTED=Color.rgb(120,135,127), GREEN=Color.rgb(67,220,170);
    private LinearLayout root; private SleepCalendar calendar; private TextView summary, dateLabel; private LocalDate selected=LocalDate.now();
    private android.content.SharedPreferences prefs;
    private int dp(float v){return (int)(getResources().getDisplayMetrics().density*v+0.5f);}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);prefs=getSharedPreferences("sleep",MODE_PRIVATE);build();}
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private void build(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(22),dp(28),dp(22),dp(30));scroll.addView(root);setContentView(scroll);
        TextView brand=text("SLEEPTRACK  /  SLEEP JOURNAL",12,GREEN);brand.setLetterSpacing(.16f);root.addView(brand);
        TextView title=text(String.valueOf(Year.now().getValue()),52,TEXT);title.setTypeface(null,1);root.addView(title);
        summary=text("",14,MUTED);root.addView(summary);
        calendar=new SleepCalendar(this);root.addView(calendar,new LinearLayout.LayoutParams(-1,dp(310)));
        TextView legend=text("●  <6h       ●  6–7h       ●  7–8h       ●  8–9h       ●  9h+",13,MUTED);root.addView(legend);
        View space=new View(this);root.addView(space,new LinearLayout.LayoutParams(1,dp(30)));
        dateLabel=text("",22,TEXT);dateLabel.setTypeface(null,1);root.addView(dateLabel);
        TextView help=text("Нажми на дату в календаре или выбери день ниже",13,MUTED);root.addView(help);
        Button choose=new Button(this);choose.setText("ВЫБРАТЬ ДАТУ");root.addView(choose);choose.setOnClickListener(v->{DatePickerDialog d=new DatePickerDialog(this,(picker,y,m,day)->{selected=LocalDate.of(y,m+1,day);update();},selected.getYear(),selected.getMonthValue()-1,selected.getDayOfMonth());d.show();});
        Button add=new Button(this);add.setText("ЗАПИСАТЬ СОН");add.setTextColor(BG);add.setBackgroundTintList(android.content.res.ColorStateList.valueOf(GREEN));root.addView(add);add.setOnClickListener(v->editSleep());
        Button clear=new Button(this);clear.setText("УДАЛИТЬ ЗАПИСЬ ЗА ДЕНЬ");root.addView(clear);clear.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Удалить запись за "+selected+"?").setNegativeButton("Отмена",null).setPositiveButton("Удалить",(d,w)->{prefs.edit().remove(selected.toString()).apply();update();}).show());
        update();
    }
    private void update(){dateLabel.setText(selected.format(DateTimeFormatter.ofPattern("d MMMM yyyy",new Locale("ru"))));int count=0;float sum=0;for(int i=1;i<=Year.now().length();i++){String value=prefs.getString(Year.now().atDay(i).toString(),"");if(!value.isEmpty()){try{sum+=Float.parseFloat(value);count++;}catch(Exception ignored){}}}summary.setText(count==0?"Пока нет записей сна":String.format(Locale.US,"%d ночей  ·  в среднем %.1f ч",count,sum/count));calendar.invalidate();}
    private void editSleep(){LinearLayout panel=new LinearLayout(this);panel.setPadding(dp(20),dp(10),dp(20),0);panel.setOrientation(1);TextView desc=text("Введи время отхода ко сну и пробуждения",15,Color.DKGRAY);panel.addView(desc);TimePicker sleep=new TimePicker(this),wake=new TimePicker(this);sleep.setIs24HourView(true);wake.setIs24HourView(true);sleep.setHour(23);sleep.setMinute(0);wake.setHour(7);wake.setMinute(0);panel.addView(text("Заснул",17,Color.DKGRAY));panel.addView(sleep);panel.addView(text("Проснулся",17,Color.DKGRAY));panel.addView(wake);ScrollView sc=new ScrollView(this);sc.addView(panel);new AlertDialog.Builder(this).setTitle("Сон · "+selected).setView(sc).setNegativeButton("Отмена",null).setPositiveButton("Сохранить",(d,w)->{int a=sleep.getHour()*60+sleep.getMinute(),b=wake.getHour()*60+wake.getMinute();int minutes=(b-a+1440)%1440;if(minutes==0){Toast.makeText(this,"Укажи разное время",Toast.LENGTH_LONG).show();return;}prefs.edit().putString(selected.toString(),String.valueOf(minutes/60f)).apply();update();}).show();}
    private class SleepCalendar extends View {
        Paint p=new Paint(3);float cell,gap,startX,startY;int year=Year.now().getValue();
        SleepCalendar(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        void label(Canvas c,String s,float x,float y,int size,int color){p.setColor(color);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));p.setTextSize(dp(size));c.drawText(s,x,y,p);}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth();cell=dp(7.5f);gap=dp(3.5f);startX=dp(38);startY=dp(62);float step=cell+gap;int weeks=(int)((LocalDate.of(year,1,1).getDayOfWeek().getValue()-1+Year.of(year).length()+6)/7);if(startX+weeks*step>w){step=(w-startX-dp(3))/weeks;cell=Math.max(dp(3),step-dp(2));}
            label(c,"MON",dp(1),startY+dp(6),10,MUTED);label(c,"WED",dp(1),startY+2*step+dp(6),10,MUTED);label(c,"FRI",dp(1),startY+4*step+dp(6),10,MUTED);label(c,"SUN",dp(1),startY+6*step+dp(6),10,MUTED);
            int offset=LocalDate.of(year,1,1).getDayOfWeek().getValue()-1;int prevMonth=0;
            for(int i=1;i<=Year.of(year).length();i++){LocalDate d=LocalDate.ofYearDay(year,i);int index=offset+i-1;int col=index/7,row=index%7;float x=startX+col*step,y=startY+row*step;if(d.getMonthValue()!=prevMonth){label(c,d.getMonth().getDisplayName(java.time.format.TextStyle.SHORT,Locale.ENGLISH),x,startY-dp(15),10,MUTED);prevMonth=d.getMonthValue();}String val=prefs.getString(d.toString(),"");int color=Color.rgb(22,32,28);if(!val.isEmpty()){try{float h=Float.parseFloat(val);color=h<6?0xff8b2934:h<7?0xff235d50:h<8?0xff319c80:h<9?0xff4bdbab:0xffd49c4d;}catch(Exception ignored){}}p.setColor(color);c.drawRoundRect(x,y,x+cell,y+cell,cell/2,cell/2,p);if(d.equals(selected)){p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));c.drawRoundRect(x-dp(1.5f),y-dp(1.5f),x+cell+dp(1.5f),y+cell+dp(1.5f),cell/2,cell/2,p);p.setStyle(Paint.Style.FILL);}}
            label(c,"Tap a day to edit your sleep",dp(2),getHeight()-dp(16),12,MUTED);
        }
        @Override public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()==MotionEvent.ACTION_UP){float step=cell+dp(3.5f);int weeks=(int)((LocalDate.of(year,1,1).getDayOfWeek().getValue()-1+Year.of(year).length()+6)/7);if(startX+weeks*step>getWidth())step=(getWidth()-startX-dp(3))/weeks;int col=(int)((e.getX()-startX)/step),row=(int)((e.getY()-startY)/step);int day=col*7+row-(LocalDate.of(year,1,1).getDayOfWeek().getValue()-1)+1;if(col>=0&&row>=0&&row<7&&day>=1&&day<=Year.of(year).length()){selected=LocalDate.ofYearDay(year,day);update();}return true;}return true;}
    }
}
