# 接口文档
Author:js00070


请求参数与返回参数均在http body中，且为json字符串

## 注册
url: /user/register

方法: POST

请求参数:
- username 用户名
- passwd 密码

返回参数:
- code 0表示成功，-1表示失败
- msg 成功或错误信息

## 登陆
url: /user/login

方法: POST

请求参数:
- username 用户名
- passwd 密码

返回参数:
- code 0表示成功，-1表示失败
- msg 成功或错误信息
- token 登陆成功后返回的token字符串

## 上传文件hash
url: /file/hash

方法: POST，header中带有token字段

请求参数:
- filename 文件名
- hash 文件hash串
- timestamp 文件创建的unix秒时间戳

返回参数:
- code 0表示成功，-1表示失败
- msg 成功或错误信息

## 文件切片上传
url: /file/upload

方法: POST，header中带有token字段

请求参数:
- filename 文件名
- total 总切片个数
- index 当前切片编号(从0开始算)
- slice 切片数据(base64编码)，数据大小随意，但不要超过64KB

返回参数:
- code 0表示成功，-1表示失败
- msg 成功或错误信息

## 获取已上传文件列表
url: /filelist

方法: GET，header中带有token字段

请求参数: 无

返回参数:
- code 0表示成功，-1表示失败
- msg 成功或错误信息
- filelist: 列表
- filename 文件名
- timestamp 创建时间戳
- count 切片数量