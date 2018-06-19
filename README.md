# RAP ![](https://api.travis-ci.org/thx/RAP.svg)

### What is RAP?

RAP is a web tool that allows web applcation developers to rapidly define and document web APIs that are used in typical RESTful-API based web applications. RAP can also be used to generate API mock data and run API services to enable front-end developers in writing and testing their front-end code that makes consumpiont of the predefined web API loaded with mock data, hence reducing the dependency from the backend development work. Meanwhile, backend developers can implement their backend code according to the defintion of API on their own pace to meet the quality and timeline requirements. With RAP, you can really do more with less.

RAP通过GUI工具帮助WEB工程师更高效的管理接口文档，同时通过分析接口结构自动生成Mock数据、校验真实接口的正确性，使接口文档成为开发流程中的强依赖。有了结构化的API数据，RAP可以做的更多，而我们可以避免更多重复劳动。

<img src="http://gtms04.alicdn.com/tps/i4/TB19tgUKVXXXXXAXXXXAhCB5VXX-1222-646.png" width="600" />


### Why we use RAP?
* Enterprise-level application: 350+ corporations including Alibaba Group have adopted RAP to manage their important API Docs and development effort!
* Fast and responsive technical support with continuous update. Go to issues list to find out how active the community is!
* Free and open source: freedom is in your hand!

### 快速上手 quick guide
* English: [Quick Guide Manual](https://github.com/thx/RAP/wiki/quick_guide) at first.
* 中文：[Video Tutorial 视频教程](http://thx.github.io/RAP/study.html)

### 其它LINKS
* 我想大概了解RAP => [Official Site 官网](http://thx.github.io/RAP)
* 我想查找详细的文档资料 => [Wiki/Documents/Manual 文档/手册](http://github.com/thx/RAP/wiki)
* 我想快速了解什么是RAP => [视频介绍](http://vodcdn.video.taobao.com/player/ugc/tb_ugc_pieces_core_player_loader.swf?version=1.0.20150330&vid=11622279&uid=11051796&p=1&t=1&rid=&random=6666)
* 我想快速上手RAP使用方法 => [视频教程](http://thx.github.io/RAP/study.html)

### Architecture
* Frontend: Velocity + jQuery + qUnit
* Backend: Hibernate5 + Spring4 + Struts2
* Data Store: MySQL5 + Redis3
* Deployment: Tomcat + Docker
* CI: Travis
