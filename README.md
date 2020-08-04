RemoteLearningClient
===
此專案開發餘Fire table上，主要為了在兒童教育上提供使用者一種多台平板電腦之間的互動。
當中包括了，線上學習、遊戲......等等的互動
連線上使用了BLE做為兩台平板之間的溝通橋樑，UI的部分加入了Lottie。而專案最困難的部分為兩台電腦中間溝通複雜的訊號。
  此專案分為上半部(此git) 以及下半部(https://github.com/mick60316/RemoteLearningServer)

0729  第一版 四種mode製作
---
Show mode
Zoom
線上教學+截圖
遊戲互動
  
0803 修改部分bug
---
# Show mode:

底圖畫面有白線
課程時間觸發切換至Show mode時即更新
Lottie animation update
Light sensor 敏感度下降為10

# Zoom mode :
    按下Start 即馬上跳轉至課程
    新增Alexa Ok 以及 Alexa Start 音效
    下半部的平板切換成Note
  
# Class mode:
    手繪的範圍修改
    所有的mode能夠透過Manubar切換
    修正截圖後再次進入時會閃爍的bug
    
# Game mode:
    新增Alexa Ok 以及 Alexa Start 音效
    
# Normal :
    hide 下半部的navigation bar 
   
    
  
  
 
