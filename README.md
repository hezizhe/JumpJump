# 微信小程序跳一跳外挂
可适用于各种大小的手机屏幕，适用于安卓手机或电脑端的安卓模拟器，目前测试最高一万六千多分。<br>

实现方法：<br>
1、通过adb命令控制手机截屏，并将图片上传到程序目录下；<br>
2、通过图像识别算法找到下一个目标方块中心点与角色人物的位置，计算二者之间的距离；<br>
3、通过距离计算出按压屏幕的时间；<br>
4、通过adb命令控制按压手机屏幕，即可完成角色人物的跳跃动作。<br>

图像识别算法：<br>
将截屏图片放大可以看见图片由像素点组成，通过像素点可以构成一个1080*1090（根据手机分辨率决定）的坐标系<br>
![](https://github.com/hezizhe/JumpJump/blob/master/%E8%AE%B2%E8%A7%A3%E5%9B%BE/01.png)<br><br>
以圆形目标方块为例：逐行获取每个像素的颜色进行解析，获取到第一个颜色区别较大的点通常就是目标方块的顶部，即图中m点的位置。（如果角色人物比目标方块高的话先找到的将是角色头部的位置，此时就标记并绕过角色继续找目标方块）接下来分两步：<br>
1、从m点开始往右边找，找到最后一个与m点像素颜色相同的点即n点，对m点和n点的横坐标取平均值，即得到上顶点o的坐标位置；<br><br>
2、从m点往左边找与m点颜色相同的左边点，优先找左边的像素点（即a点），如果颜色不满足再找左下角的像素点（即b点），如果还不满足就找下边的一个点（即c点）。例图中最先满足条件的为b点。<br>
![](https://github.com/hezizhe/JumpJump/blob/master/%E8%AE%B2%E8%A7%A3%E5%9B%BE/02.png)<br><br>
以递归的方式按照上边的方法从b点继续找下去，会发现此时在做的好比一个描边的动作<br>
![](https://github.com/hezizhe/JumpJump/blob/master/%E8%AE%B2%E8%A7%A3%E5%9B%BE/03.png)<br><br>
按照此方式将会一直找到图中的g点，此时g点的左边、左下角、下边已经都不满足条件。g点所在的纵坐标已经是目标方块最左边。接着从g点往上找到最后一个颜色相同的点h，对g和h的纵坐标取平均值，即得到左顶点k的坐标位置。<br>
![](https://github.com/hezizhe/JumpJump/blob/master/%E8%AE%B2%E8%A7%A3%E5%9B%BE/04.png)<br><br>
通过以上两步得到了上顶点o的坐标位置与左顶点k的坐标位置，取上顶点的横坐标与左顶点的纵坐标即得到目标方块中心点的坐标位置。<br>
![](https://github.com/hezizhe/JumpJump/blob/master/%E8%AE%B2%E8%A7%A3%E5%9B%BE/05.png)<br><br>
方形的目标方块边缘棱角更明显，按照上边的算法将更容易确定上顶点与左顶点<br>
![](https://github.com/hezizhe/JumpJump/blob/master/%E8%AE%B2%E8%A7%A3%E5%9B%BE/07.png)<br><br>
未避免特殊道具方块造成中心点计算不够准确的情况，程序作了双重保证。当遇到特殊方块时，程序会去寻找目标方块中心的白点（当角色命中中心点时，下一个目标方块中心点上会出现一个白色点，该程序命中中心点的概率很高，基本上都能找到白点）。继续按照上边的算法找到白点的中心点，修正以该点为目标方块的中心点。<br>
![](https://github.com/hezizhe/JumpJump/blob/master/%E8%AE%B2%E8%A7%A3%E5%9B%BE/7.png)<br><br>
提示中心点的白点的颜色几乎是唯一的，即使在白色方块上肉眼看不出来，程序依然能识别出二者rgb值的区别<br>
![](https://github.com/hezizhe/JumpJump/blob/master/%E8%AE%B2%E8%A7%A3%E5%9B%BE/08.png)<br>
![](https://github.com/hezizhe/JumpJump/blob/master/%E8%AE%B2%E8%A7%A3%E5%9B%BE/09.png)<br><br>
同样的方法找到角色人物上顶点的位置，由于角色形状固定，直接以上顶点按照特定比例往下移即可找到人物底部的中心点位置坐标。通过目标方块的中心点和人物的中心点坐标即可计算出二者之间的距离。距离乘以弹跳系数即可计算出按压屏幕的时间<br>
![](https://github.com/hezizhe/JumpJump/blob/master/%E8%AE%B2%E8%A7%A3%E5%9B%BE/97.png)<br>
