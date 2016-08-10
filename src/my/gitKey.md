# gitKey
http://www.ruanyifeng.com/blog/2015/12/git-cheat-sheet.html
http://www.cnblogs.com/cspku/articles/Git_cmds.html
http://blog.csdn.net/dengsilinming/article/details/8000622


//初始化必须的命令
git config --global user.name "NAME"
git config --global user.email "EMAIL"
git init

//记得更改目录,否则clone等操作都会在当前目录进行!
#查看当前目录
pwd
#在当前目录新建文件夹
mkdir directoryName

//克隆远程仓库(目录)到本地(当前文件夹)
git clone https://github.com/Victor-Lv/gitKey.git

//记得把当前目录切换到仓库目录下,不然会发现很多命令都没反应
cd gitKey


//查看git状态
git status