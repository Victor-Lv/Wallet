# Wallet
这是自己写的一个javacard 电子钱包的小示例，比较简单。初版本（version-1.0）可以通过全部初级测试（包括基本功能）。

Version 1.0：最简单的电子钱包Applet示例。自己写的一个applet，pin功能是自己实现的，是为了让东西有些难度，一般实际应用中使用的是javacard官方的pin类库（OwnerPin）。
具有验证pin（包括尝试次数限制以及锁卡）、修改pin、充值、消费、查询余额等基本的小功能。
两个java文件为Applet的java文件，jcsh文件是一两句测试样例，png截图是用Python脚本测试一大堆基本功能全部pass的情况，因为测试工具和测试样例都是前辈给的，版权原因，这里我只上传我自己写的Applet代码。
